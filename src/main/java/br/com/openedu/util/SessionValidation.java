package br.com.openedu.util;

import br.com.openedu.dao.MemberDAO;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import br.com.openedu.dao.SessionDAO;
import br.com.openedu.model.Session;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

public abstract class SessionValidation {

    private final SessionDAO sessionDAO;
    protected final MemberDAO memberDAO;

    public SessionValidation() {
        sessionDAO = new SessionDAO();
        memberDAO = new MemberDAO();
    }

    public Session validateSession(String sessionId) throws MongoException {

        Session session = null;
        DBCursor cursor = sessionDAO.find(UUID.fromString(sessionId), new Date());

        if (cursor.count() == 1) {

            session = new Session();
            session.putAll(cursor.next());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 60);
            session.setExpirationDate(calendar.getTime());
            sessionDAO.update(new BasicDBObject("_id", session.getObjectId("_id")), session);

        }

        return session;
    }
    
    
    protected BasicDBObject byEmail(String email) {
        return new BasicDBObject("email", email);
    }

}
