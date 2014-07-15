package br.com.openedu.rest;

import java.util.Calendar;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import br.com.openedu.dao.MemberDAO;
import br.com.openedu.dao.SessionDAO;
import br.com.openedu.model.Codes;
import br.com.openedu.model.Member;
import br.com.openedu.model.Session;
import br.com.openedu.util.SessionValidation;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

@Path("member")
public class MemberService extends SessionValidation {

	private final Gson gson;
	private final MemberDAO memberDAO;
	private final SessionDAO sessionDAO;
	private final Session session;
	private final BasicDBObject result;
	private static String EMAIL_REG = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

	public MemberService() {
		gson = new Gson();
		memberDAO = new MemberDAO();
		sessionDAO = new SessionDAO();
		session = new Session();
		result = new BasicDBObject();
	}

	/**
	 * Method that receives a JSON content that represents a Member object,
	 * converts it to a Member object and writes to the database.
	 * 
	 * @param content
	 *            - The content to JSON conversion.
	 * @return {@link Response}
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(String content) {

		Member member = memberFrom(content);

		try {

			if (validateInformationsToCreate(member)) {
				memberDAO.create(member);
				result.put("code", Codes.SUCCESSFULL_CREATED);
				member.setPassword(null);
				result.put("entity", member);
				return responseOk();
			} else {
				return responseUnauthorized();
			}

		} catch (MongoException exception) {
			return exceptionMessage(exception);
		}

	}

	@POST
	@Path("/session")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticate(String content) {

		Member member = memberFrom(content);

		try {

			DBCursor cursor = memberDAO.find(member);
			if (existsOneObjectIn(cursor)) {
				member.putAll(cursor.next());
				result.put("code", Codes.SESSION_CREATED);
				session.setSessionId(UUID.randomUUID());
				member.remove("password");
				member.remove("dropBoxToken");
				session.setMember(member);
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MINUTE, 60);
				session.setExpirationDate(calendar.getTime());
				sessionDAO.create(session);
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response me(@PathParam("sessionId") String sessionId) {

		try {

			Session session = validateSession(sessionId);

			if (session == null) {
				result.put("code", Codes.NOT_EXISTS_SESSION);
				return responseUnauthorized();
			}
			
			result.put("code", Codes.EXISTS_SESSION);
			result.put("entity", session);
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

		Session session = validateSession(sessionId);

		if (session == null) {
			result.put("code", Codes.NOT_EXISTS_SESSION);
			return responseUnauthorized();
		}

		try {

			Member member = memberFrom(contentForUpdate);
			DBCursor cursor = memberDAO.find(byEmail(member));
			removeNotUpdateableFields(member);

			if (existsOneObjectIn(cursor)) {

				DBObject data = cursor.next();
				data.putAll(member.toMap());
				member.putAll(data);
				memberDAO.updateByEmail(member);
				session.setMember(member);
				sessionDAO.updateBySessionId(session);
				result.put("code", Codes.MEMBER_UPDATED);
				result.put("entity", member);
				return responseOk();

			} else {
				result.put("code", Codes.NOT_EXISTS_MEMBER);
				return responseOk();
			}

		} catch (MongoException exception) {
			return exceptionMessage(exception);
		}

	}

	private boolean existsOneObjectIn(DBCursor cursor) {
		return (cursor.count() == 1);
	}

	private BasicDBObject byEmail(Member member) {
		return new BasicDBObject("email", member.getEmail());
	}

	private void removeNotUpdateableFields(Member member) {
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

	private Response exceptionMessage(MongoException exception) {
		result.put("code", Codes.MONGODB_EXCEPTION);
		result.put("message", gson.fromJson(exception.getMessage(), BasicDBObject.class).get("err"));
		return responseOk();
	}

	private boolean validateInformationsToCreate(Member member) {

		BasicDBList validations = new BasicDBList();

		if (member.getEmail() == null || member.getEmail().isEmpty()) {
			validations.add(Codes.EMAIL_CANT_BE_EMPTY_OR_NULL);
		}

		if (member.getName() == null || member.getName().isEmpty()) {
			validations.add(Codes.NAME_CANT_BE_EMPTY_OR_NULL);
		}

		if (!member.getEmail().matches(EMAIL_REG)) {
			validations.add(Codes.EMAIL_FORMAT_NOT_MATCH);
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

}
