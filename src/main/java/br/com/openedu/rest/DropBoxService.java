package br.com.openedu.rest;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import br.com.openedu.dao.SessionDAO;
import br.com.openedu.model.Codes;
import br.com.openedu.model.Member;
import br.com.openedu.model.Session;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxPath;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Path("/storage/dropbox")
public class DropBoxService {

	private final BasicDBObject result;
	private final SessionDAO sessionDAO;
	private final Session session;
	private final Gson gson; 
	
	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;
	
	public DropBoxService() {
		result = new BasicDBObject();
		sessionDAO = new SessionDAO();
		session = new Session();
		gson = new GsonBuilder().disableHtmlEscaping().create();
	}

	@GET
	@Path("/authorization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAuthorization() {

		try {

			DbxWebAuthNoRedirect webAuth = getWebAuth();
			String authorizeUrl = webAuth.start();
			result.put("authorizeURL", authorizeUrl);
			return responseOk();

		} catch (Exception exception) {
			return exceptionGenericMessage(exception);
		}

	}

	@GET
	@Path("/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response linkUser(@PathParam("code") String code) {

		try {
			DbxWebAuthNoRedirect webAuth = getWebAuth();
			DbxAuthFinish authFinish = webAuth.finish(code);
			String accessToken = authFinish.accessToken;
			result.put("accessToken", accessToken);
			return responseOk();
		} catch (Exception exception) {
			return exceptionGenericMessage(exception);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/member/{sessionId}/file")
	public Response postFile(@PathParam("sessionId") String sessionId, @QueryParam("path") String path, InputStream inputStream) {

		try {

			session.put("sessionId", UUID.fromString(sessionId));
			DBCursor cursor = sessionDAO.find(session);
			
			if (cursor.count() == 1) {
				Member member = memberFrom(cursor);
				DbxClient dbxClient = new DbxClient(getRequestConfig(request), member.getDropBoxToken());
				try {
					dbxClient.uploadFile(path, DbxWriteMode.add(), request.getContentLength(), inputStream);
				} finally {
					inputStream.close();
				}
			} else {
				result.put("code", Codes.NOT_EXISTS_SESSION);
			}

			return responseOk();

		} catch (Exception exception) {
			return exceptionGenericMessage(exception);
		}
	}

	private Response exceptionGenericMessage(Exception exception) {
		result.put("message", exception.getMessage());
		return responseOk();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/member/{sessionId}")
	public Response getPath(@PathParam("sessionId") String sessionId, @QueryParam("path") String path) {

		try {
			if (path == null) {
				path = "/";
			} else {
				String pathError = DbxPath.findError(path);
				if (pathError != null) {
					result.put("code", Codes.BAD_FORMATTED_DROPBOX_PATH);
					throw new Exception("Bad Formatted Path.");
				}
			}

			session.put("sessionId", UUID.fromString(sessionId));
			DBCursor cursor = sessionDAO.find(session);

			if (cursor.count() == 1) {
				Member member = memberFrom(cursor);
				if (member.getDropBoxToken() == null) {
					result.put("code", Codes.TOKEN_DROPBOX_NOT_FOUND);
					throw new Exception("Token not found.");
				}
				DbxClient dbxClient = new DbxClient(getRequestConfig(request), member.getDropBoxToken());
				DbxEntry.WithChildren listing;
				listing = dbxClient.getMetadataWithChildren(path);
				result.put("entity", listing);
			} else {
				result.put("code", Codes.NOT_EXISTS_SESSION);
			}

			return responseOk();

		} catch (Exception exception) {
			return exceptionGenericMessage(exception);
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/member/{sessionId}/file")
	public Response getFile(@PathParam("sessionId") String sessionId, @QueryParam("path") String path) {

		try {

			session.put("sessionId", UUID.fromString(sessionId));
			DBCursor cursor = sessionDAO.find(session);

			if (cursor.count() == 1) {
				Member member = memberFrom(cursor);
				if (member.getDropBoxToken() == null) {
					result.put("code", Codes.TOKEN_DROPBOX_NOT_FOUND);
					throw new Exception("Token not found.");
				}
				DbxClient dbxClient = new DbxClient(getRequestConfig(request), member.getDropBoxToken());
				dbxClient.getFile(path, null, response.getOutputStream());
			} else {
				result.put("code", Codes.NOT_EXISTS_SESSION);
			}

			return responseOk();

		} catch (Exception exception) {
			return exceptionGenericMessage(exception);
		}

	}

	private Member memberFrom(DBCursor cursor) {
		Member member = new Member();
		member.putAll((DBObject) cursor.next().get("member"));
		return member;
	}

	private DbxRequestConfig getRequestConfig(HttpServletRequest request) {
		return new DbxRequestConfig("OpenEdu/1.0", request.getLocale().toString());
	}

	private Response responseOk() {
		return Response.status(Status.OK).entity(gson.toJson(result)).build();
	}

	private DbxWebAuthNoRedirect getWebAuth() {

		DbxAppInfo appInfo = new DbxAppInfo("8q6ap39ipefwg8m", "y5qoyr762e37qch");
		DbxRequestConfig config = new DbxRequestConfig("OpenEdu/1.0", Locale.getDefault().toString());

		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		return webAuth;
	}
}
