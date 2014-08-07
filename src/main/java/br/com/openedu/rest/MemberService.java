package br.com.openedu.rest;

import br.com.openedu.dao.SessionDAO;
import br.com.openedu.model.Codes;
import br.com.openedu.model.Image;
import br.com.openedu.model.Member;
import br.com.openedu.model.Session;
import br.com.openedu.util.SessionValidation;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.apache.commons.codec.binary.Base64;

@Path("member")
public class MemberService extends SessionValidation {

    private final Gson gson;
    private final SessionDAO sessionDAO;
    private final Session session;
    private final BasicDBObject result;
    private static String EMAIL_REG = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    @Context
    HttpServletResponse response;

    public MemberService() {
        gson = new Gson();
        sessionDAO = new SessionDAO();
        session = new Session();
        result = new BasicDBObject();
    }

    /**
     * Method that receives a JSON content that represents a Member object,
     * converts it to a Member object and writes to the database.
     *
     * @param content - The content to JSON conversion.
     * @return {@link Response}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(String content) {

        Member member = memberFrom(content);

        try {

            if (validateInformationsToCreate(member)) {

                Date now = new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTime();
                member.setSince(now);
                member.setEnabled(false);
                final SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                member.setBirthDate(format.parse(member.getString("birthDate")));
                memberDAO.create(member);
                result.put("code", Codes.SUCCESSFULL_CREATED);
                return responseOk();

            } else {
                return responseUnauthorized();
            }

        } catch (MongoException | ParseException exception) {
            return exceptionGenericMessage(exception);
        }

    }

    @POST
    @Path("/session")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(String content) {

        Member member = memberFrom(content);

        try {

            member.setEnabled(true);
            DBCursor cursor = memberDAO.find(member);

            if (existsOneObjectIn(cursor)) {

                member.putAll(cursor.next());
                session.setSessionId(UUID.randomUUID());
                member.remove("password");
                member.remove("dropBoxToken");
                session.setMember(member.getEmail());
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 60);
                session.setExpirationDate(calendar.getTime());
                sessionDAO.create(session);
                result.put("code", Codes.SESSION_CREATED);
                result.put("entity", session);
                return responseOk();

            } else {
                result.put("code", Codes.NOT_EXISTS_MEMBER);
                return responseOk();
            }

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }
    }

    @GET
    @Path("/me/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@PathParam("sessionId") String sessionId) {

        try {

            Session _session = validateSession(sessionId);

            if (_session == null) {
                result.put("code", Codes.NOT_EXISTS_SESSION);
                return responseOk();
            }

            Member member = new Member();
            member.putAll(memberDAO.find(byEmail(_session.getMember())).next());
            member.remove("password");
                        
            result.put("code", Codes.EXISTS_SESSION);
            result.put("entity", member);
            return responseOk();

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }
    }

    @PUT
    @Path("/me/{sessionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("sessionId") String sessionId, String contentForUpdate) {

        Session _session = validateSession(sessionId);

        if (_session == null) {
            result.put("code", Codes.NOT_EXISTS_SESSION);
            return responseUnauthorized();
        }

        try {

            Member member = memberFrom(contentForUpdate);
            return update(member, _session);

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }

    }

    private Response update(Member member, Session _session) throws MongoException {
        DBCursor cursor = memberDAO.find(byEmail(member.getEmail()));
        removeNotUpdateableFields(member);

        if (existsOneObjectIn(cursor)) {

            DBObject data = cursor.next();
            data.putAll(member.toMap());
            member.putAll(data);
            memberDAO.updateByEmail(member);
            result.put("code", Codes.MEMBER_UPDATED);
            result.put("entity", member);
            return responseOk();

        } else {
            result.put("code", Codes.NOT_EXISTS_MEMBER);
            return responseOk();
        }
    }

    @POST
    @Path("/photo/{sessionId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response setPhoto(@PathParam("sessionId") String sessionId, String streamImage) {

        try {
            Session _session = validateSession(sessionId);

            if (_session == null) {
                result.put("code", Codes.NOT_EXISTS_SESSION);
                return responseUnauthorized();
            }

            Member member = new Member();
            member.putAll(memberDAO.find(byEmail(_session.getMember())).next());

            GridFS gridFS = memberDAO.getGridFS(member.getObjectId("_id").toHexString());

            if (member.getImage() != null) {
                String image = member.getImage();
                GridFSDBFile file = gridFS.findOne(image);
                if (file != null) {
                    gridFS.remove(file);
                }
            }

            byte[] bytes = decodeImage(streamImage);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            BufferedImage imageResized = Scalr.resize(ImageIO.read(inputStream),800,Scalr.OP_ANTIALIAS);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(imageResized, "png", output);
            InputStream input = new ByteArrayInputStream(output.toByteArray());

            String imageId = UUID.randomUUID().toString();
            
            Image image = new Image(gridFS, input, imageId);
            image.save();

            member.setImage(imageId);
            return update(member, _session);

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        } catch (IOException ex) {
            return exceptionGenericMessage(ex);
        }
    }

    @GET
    @Path("/photo/{sessionId}/{imageId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPhoto(@PathParam("sessionId") String sessionId, @PathParam("imageId") String imageId) throws IOException {

        Session _session = validateSession(sessionId);

        if (_session == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        Member member = new Member();
        member.putAll(memberDAO.find(byEmail(_session.getMember())).next());

        try {

            GridFS gridFS = memberDAO.getGridFS(member.getObjectId("_id").toHexString());
            GridFSDBFile imageForOutput = gridFS.findOne(imageId);

            if (imageForOutput == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            final InputStream inputStream = imageForOutput.getInputStream();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(IOUtils.toByteArray(inputStream));
            stream.writeTo(response.getOutputStream());
            
            return responseOk();

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        } 
    }

    private boolean existsOneObjectIn(DBCursor cursor) {
        return (cursor.count() == 1);
    }

    private void removeNotUpdateableFields(Member member) {
        member.remove("_id");
        member.remove("tag");
        member.remove("email");
        member.remove("role");
    }

    private Member memberFrom(String content) {
        return gson.fromJson(content, Member.class);
    }

    private Response responseOk() {
        return Response.status(Status.OK).entity(gson.toJson(result)).build();
    }

    private Response responseUnauthorized() {
        return Response.status(Status.UNAUTHORIZED).entity(gson.toJson(result)).build();
    }

    private Response exceptionGenericMessage(Exception exception) {
        result.put("message", exception.getMessage());
        return responseOk();
    }

    private Response exceptionMessage(MongoException exception) {
        result.put("code", Codes.MONGODB_EXCEPTION);
        result.put("message", gson.fromJson(exception.getMessage(), BasicDBObject.class).get("err"));
        return responseOk();
    }

    private boolean validateInformationsToCreate(Member member) {

        BasicDBList validations = new BasicDBList();

        if (member.getEmail() == null || member.getEmail().isEmpty()) {
            validations.add(Codes.EMAIL_CANT_BE_EMPTY_OR_NULL);
        } else if (!member.getEmail().matches(EMAIL_REG)) {
            validations.add(Codes.EMAIL_FORMAT_NOT_MATCH);
        }

        if (member.getName() == null || member.getName().isEmpty()) {
            validations.add(Codes.NAME_CANT_BE_EMPTY_OR_NULL);
        }

        if (memberDAO.exists("email", member.getEmail())) {
            validations.add(Codes.EMAIL_ALREADY_EXISTS);
        }

        if (memberDAO.exists("tag", member.getTag())) {
            validations.add(Codes.TAG_ALREADY_EXISTS);
        }

        if (validations.isEmpty()) {
            return true;
        } else {
            result.put("code", Codes.CONTAINS_ERRORS);
            result.put("validations", validations);
            return false;
        }

    }
    
    private byte[] decodeImage(String imageDataString){
        return Base64.decodeBase64(imageDataString);
    }

}
