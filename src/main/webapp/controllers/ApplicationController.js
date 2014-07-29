var ApplicationCotroller = openedu.controller('ApplicationController', ['$scope', 'Templates', 'MemberFactory', 'Codes',
    function ApplicationController($scope, Templates, MemberFactory, Codes) {

        $scope.Templates = Templates;
        $scope.template;
        this.memberSignIn = {};
        this.memberSignUp = {};
        this.birthDate = {
            day: null,
            month: null,
            year: null
        };

        this.inProgress = false;

        if (sessionStorage.session !== undefined) {

            var session = angular.fromJson(sessionStorage.session);
            getMe(session.sessionId);

        }
        else {
            $scope.template = Templates.HOME;
        }

        this.signIn = function() {

//            this.inProgress = true;

            var params = {
                controller: this,
                data: this.memberSignIn,
                success: function(result) {

                    if (result.code === Codes.SESSION_CREATED) {

                        var session = result.entity;
                        sessionStorage.session = angular.toJson(session);
                        getMe(session.sessionId);
                        $scope.template = Templates.USER_AREA_HOME;

                    } else if (result.code === Codes.NOT_EXISTS_MEMBER) {

                    }

//                    this.controller.inProgress = false;

                },
                error: function() {
                    this.controller.inProgress = false;
                }
            };

            MemberFactory.signIn(params);

        };

        this.signUp = function() {
            var params = {
                controller: this,
                data: this.memberSignUp,
                success: function(result) {

                    if (result.code === Codes.SESSION_CREATED) {

                        sessionStorage.session = angular.toJson(result.entity);
                        $scope.template = Templates.USER_AREA_HOME;

                    } else if (result.code === Codes.NOT_EXISTS_MEMBER) {

                    }
                },
                error: function() {

                }
            };

            MemberFactory.signUp(params);
        };

        function getMe(sessionId) {
            var params = {
                sessionId: sessionId,
                success: function(result) {

                    if (result.code === Codes.EXISTS_SESSION) {

                        $scope.member = result.entity;
                        $scope.profileImage = MemberFactory.getPhoto($scope.member.image, this.sessionId);
                        $scope.template = Templates.USER_AREA_HOME;

                    }
                    else if (result.code === Codes.NOT_EXISTS_SESSION) {
                        $scope.template = Templates.HOME;
                    }

                },
                error: function() {
                    $scope.template = Templates.HOME;
                }
            };

            MemberFactory.me(params);

        }

    }
]);