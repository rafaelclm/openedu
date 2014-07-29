package br.com.openedu.rest;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import br.com.openedu.dao.TutorialDAO;
import br.com.openedu.model.Codes;
import br.com.openedu.model.Image;
import br.com.openedu.model.Member;
import br.com.openedu.model.Session;
import br.com.openedu.model.Tutorial;
import br.com.openedu.model.Tutorial.Category;
import br.com.openedu.util.HttpConfig;
import br.com.openedu.util.SessionValidation;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

@Path("/tutorial")
public class TutorialService extends SessionValidation {

    private TutorialDAO tutorialDAO;
    private Gson gson;
    private BasicDBObject result;
    private HttpClient httpClient;
    private static final String TUTORIAL_IMAGE_STORE = "TUTORIAL_IMAGE_STORE";

    @Context
    HttpServletResponse response;

    public TutorialService() {
        tutorialDAO = new TutorialDAO();
        gson = new Gson();
        result = new BasicDBObject();
        httpClient = HttpClientBuilder.create().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postTutorial(String content) {

        Tutorial tutorial = tutorialFrom(content);

        Session session = validateSession(tutorial.getSessionId());
        Member member;

        if (session == null) {
            result.put("code", Codes.NOT_EXISTS_SESSION);
            return responseUnauthorized();
        } else {

            member = new Member();
            member.putAll(memberDAO.find(byEmail(session.getMember())).next());
            tutorial.setAuthor(member.getObjectId("_id"));
        }

        try {

            if (validateInformationsToCreate(tutorial)) {

                if (existsImagePathDropBox(tutorial)) {
                    getImageDropBox(tutorial, member);
                }

                Date now = new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTime();
                tutorial.setCreationDate(now);
                tutorial.setLastUpdate(now);
                tutorialDAO.create(tutorial);
                result.put("code", Codes.SUCCESSFULL_CREATED);
                result.put("entity", tutorial);
                return responseOk();

            } else {
                return responseUnauthorized();
            }
        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }
    }

    private void getImageDropBox(Tutorial tutorial, Member member) {

        HttpUriRequest request = new HttpGet(HttpConfig
                .getBaseURI()
                .concat("storage/dropbox/member")
                .concat("/")
                .concat(tutorial.getSessionId().concat("/").concat("file").concat("?path=")
                        .concat(tutorial.getImagePathDropBox())));

        try {

            HttpResponse resp = httpClient.execute(request);
            HttpEntity entity = resp.getEntity();
            InputStream inputStream = entity.getContent();
            String imageId = saveImage(inputStream);
            tutorial.setImageId(imageId);

        } catch (IOException exception) {
            exceptionGenericMessage(exception);
        }
    }

    private String saveImage(InputStream inputStream) {
        GridFS gridFS = tutorialDAO.getGridFS(TUTORIAL_IMAGE_STORE);
        String imageId = UUID.randomUUID().toString();
        Image image = new Image(gridFS, inputStream, imageId);
        image.save();
        return imageId;
    }

    @GET
    @Path("/session/{sessionId}/image/{imageId}")
    public Response getImage(@PathParam("imageId") String imageId, @PathParam("sessionId") String sessionId) {

        Session session = validateSession(sessionId);

        if (session == null) {
            result.put("code", Codes.NOT_EXISTS_SESSION);
            return responseUnauthorized();
        }

        try {

            GridFS gridFS = tutorialDAO.getGridFS(TUTORIAL_IMAGE_STORE);
            GridFSDBFile imageForOutput = gridFS.findOne(imageId);

            final InputStream inputStream = imageForOutput.getInputStream();
            response.getOutputStream().write(IOUtils.toByteArray(inputStream));
            return responseOk();

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        } catch (IOException exception) {
            return exceptionGenericMessage(exception);
        }

    }

    @POST
    @Path("/session/{sessionId}/image")
    public Response setImage(@PathParam("sessionId") String sessionId, InputStream inputStream) {

        Session session = validateSession(sessionId);

        if (session == null) {
            result.put("code", Codes.NOT_EXISTS_SESSION);
            return responseUnauthorized();
        }

        Member member = new Member();
        member.putAll(memberDAO.find(byEmail(session.getMember())).next());

        try {
            String imageId = saveImage(inputStream);
            result.put("imageId", imageId);
            return responseOk();
        } catch (MongoException exception) {
            return exceptionMessage(exception);
        } catch (Exception exception) {
            return exceptionGenericMessage(exception);
        }
    }

    @GET
    @Path("/session/{sessionId}/skip/{skip}/limit/{limit}")
    public Response getTutorials(@PathParam("sessionId") String sessionId, @PathParam("skip") int skip,
            @PathParam("limit") int limit) {

        Session session = validateSession(sessionId);

        if (session == null) {
            result.put("code", Codes.NOT_EXISTS_SESSION);
            return responseUnauthorized();
        }

        try {

            DBCursor cursor = tutorialDAO.find(skip, limit);
            result.put("entity", cursor.toArray());
            return responseOk();

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }

    }

    @GET
    @Path("/member/session/{sessionId}/skip/{skip}/limit/{limit}")
    public Response getTutorialsByMember(@PathParam("sessionId") String sessionId, @PathParam("skip") int skip,
            @PathParam("limit") int limit) {

        Session session = validateSession(sessionId);

        if (session == null) {
            result.put("code", Codes.NOT_EXISTS_SESSION);
            return responseUnauthorized();
        }

        try {

            Member member = new Member();
            member.putAll(memberDAO.find(byEmail(session.getMember())).next());
            
            DBCursor cursor = tutorialDAO.find(member.getObjectId("_id"), skip, limit);
            result.put("entity", cursor.toArray());
            return responseOk();

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }

    }

    @GET
    @Path("/session/{sessionId}/startdate/{startdate}/enddate/{enddate}")
    public Response getTutorialsByPeriod(@PathParam("sessionId") String sessionId, @PathParam("startdate") String startdate,
            @PathParam("enddate") String enddate) {

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH-mm-ss", Locale.getDefault());

        try {

            Session session = validateSession(sessionId);

            if (session == null) {
                result.put("code", Codes.NOT_EXISTS_SESSION);
                return responseUnauthorized();
            }

            DBCursor cursor = tutorialDAO.find(format.parse(startdate), format.parse(enddate));
            result.put("entity", cursor.toArray());
            return responseOk();

        } catch (ParseException exception) {
            return exceptionGenericMessage(exception);
        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }

    }

    @GET
    @Path("/session/{sessionId}/title/{title}")
    public Response getTutorialsByTitle(@PathParam("sessionId") String sessionId, @PathParam("title") String title) {

        try {

            Session session = validateSession(sessionId);

            if (session == null) {
                result.put("code", Codes.NOT_EXISTS_SESSION);
                return responseUnauthorized();
            }

            DBCursor cursor = tutorialDAO.find(title);
            result.put("entity", cursor.toArray());
            return responseOk();

        } catch (MongoException exception) {
            return exceptionMessage(exception);
        }

    }

    @GET
    @Path("/categories")
    public Response getCategories() {

        try {
            result.put("entity", Category.values());
            return responseOk();

        } catch (Exception exception) {
            return exceptionGenericMessage(exception);
        }

    }

    private boolean existsImagePathDropBox(Tutorial tutorial) {
        return tutorial.getImagePathDropBox() != null && !tutorial.getImagePathDropBox().isEmpty();
    }

    private Tutorial tutorialFrom(String content) {
        return gson.fromJson(content, Tutorial.class);
    }

    private Response responseOk() {
        return Response.status(Status.OK).entity(gson.toJson(result)).build();
    }

    private Response exceptionGenericMessage(Exception exception) {
        result.put("message", exception.getMessage());
        return responseOk();
    }

    private Response responseUnauthorized() {
        return Response.status(Status.UNAUTHORIZED).entity(gson.toJson(result)).build();
    }

    private Response exceptionMessage(MongoException exception) {
        result.put("code", Codes.MONGODB_EXCEPTION);
        result.put("message", gson.fromJson(exception.getMessage(), BasicDBObject.class).get("err"));
        return responseOk();
    }

    private boolean validateInformationsToCreate(Tutorial tutorial) {

        BasicDBList validations = new BasicDBList();

        if (tutorial.getTitle() == null || tutorial.getTitle().isEmpty()) {
            validations.add(Codes.TITLE_CANT_BE_NULL_OR_EMPTY);
        }

        if (tutorial.getCategory() == null) {
            validations.add(Codes.CATEGORY_CANT_BE_NULL_OR_EMPTY);
        }

        if (validations.isEmpty()) {
            return true;
        } else {
            result.put("code", Codes.CONTAINS_ERRORS);
            result.put("validations", validations);
            return false;
        }
    }

}
