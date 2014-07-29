openedu.factory("MemberFactory", ['HTTP', 'HttpConfig', function MemberFactory(HTTP, HttpConfig) {

        var MemberFactory = {};

        MemberFactory.signIn = function(params) {

            var SERVICE_LOGIN = HttpConfig.getBaseURI().concat('member/session');
            params.service = SERVICE_LOGIN;
            HTTP.post(params);

        };

        MemberFactory.me = function(params) {
            var SERVICE_ME = HttpConfig.getBaseURI().concat('member/me/').concat(params.sessionId);
            params.service = SERVICE_ME;
            HTTP.get(params);
        };

        MemberFactory.signUp = function(params) {
            var SERVICE_SIGNUP = HttpConfig.getBaseURI().concat('member/');
            params.service = SERVICE_SIGNUP;
            HTTP.post(params);
        };

        MemberFactory.setPhoto = function(params) {
            var SERVICE_SET_PHOTO = HttpConfig.getBaseURI().concat('member/photo/')
                    .concat(params.sessionId);
            params.service = SERVICE_SET_PHOTO;
            params.headers = {
                'Content-Type': 'application/octet-stream'
            };
            HTTP.post(params);
        };

        MemberFactory.getPhoto = function(imageId, sessionId) {
            return HttpConfig.getBaseURI().concat('member/photo/')
                    .concat(sessionId).concat('/').concat(imageId);
        };

        return MemberFactory;

    }]);