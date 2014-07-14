package br.com.openedu.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import br.com.openedu.model.Tutorial;
import br.com.openedu.util.HttpConfig;
import br.com.openedu.util.SessionValidation;
import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

@Path("/tutorial")
public class TutorialService extends SessionValidation {

	private TutorialDAO tutorialDAO;
	private Gson gson;
	private BasicDBObject result;
	private HttpClient httpClient;
	
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

		Member member = validateSession(tutorial.getSessionId());

		if (member == null) {
			result.put("code", Codes.NOT_EXISTS_SESSION);
			return responseUnauthorized();
		} else {
			tutorial.setAuthor(member);
		}

		try {

			if (validateInformationsToCreate(tutorial)) {

				if (existsImagePathDropBox(tutorial)) {

					HttpUriRequest request = new HttpGet(HttpConfig
									.getBaseURI()
									.concat("storage/dropbox/member")
									.concat("/")
									.concat(tutorial.getSessionId().concat("/").concat("file").concat("?path=")
													.concat(tutorial.getImagePathDropBox())));

					try {

						HttpResponse response = httpClient.execute(request);
						HttpEntity entity = response.getEntity();
						InputStream inputStream = entity.getContent();
						GridFS gridFS = tutorialDAO.getGridFS(member.getObjectId("_id").toHexString());
						String filename = UUID.randomUUID().toString();
						Image image = new Image(gridFS, inputStream, filename);
						image.save();
						tutorial.setImageId(filename);

					} catch (IOException exception) {
						exceptionGenericMessage(exception);
					}
				}

				tutorial.setCreationDate(new Date());
				tutorial.setLastUpdate(new Date());
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

	@GET
	@Path("/session/{sessionId}/image/{imageId}")
	public Response getImage(@PathParam("imageId") String imageId, @PathParam("sessionId") String sessionId) {

		Member member = validateSession(sessionId);

		if (member == null) {
			result.put("code", Codes.NOT_EXISTS_SESSION);
			return responseUnauthorized();
		}

		try {

			GridFS gridFS = tutorialDAO.getGridFS(member.getObjectId("_id").toHexString());
			GridFSDBFile imageForOutput = gridFS.findOne(imageId);

			final InputStream inputStream = imageForOutput.getInputStream();
			
			response.getOutputStream().write(IOUtils.toByteArray(inputStream));;

			return responseOk();

		} catch (MongoException exception) {
			return exceptionMessage(exception);
		} catch (IOException exception) {
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

		if (tutorial.getAuthor() == null) {
			validations.add(Codes.AUTHOR_NOT_INFORMED);
		}

		return true;
	}

}
