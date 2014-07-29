openedu.controller('MemberController', ['$scope', '$rootScope', 'MemberFactory', 'Codes',
    function MemberController($scope, $rootScope, MemberFactory, Codes) {

        this.photo = {};
        $scope.session = angular.fromJson(sessionStorage.session);

        this.logOut = function() {
            sessionStorage.removeItem('session');
            sessionStorage.removeItem('member');
            $scope.template = $scope.Templates.HOME;
        };

        $scope.$watch('memberController.photo', function(photo) {

            if (!photo.file) {
                return;
            }

            $rootScope.profileImageIsLoaded = false;
            $scope.profileImage = "images/loading.gif";

            var params = {
                data: photo.file,
                sessionId: angular.fromJson(sessionStorage.session).sessionId,
                success: function(result) {
                    if (result.code === Codes.MEMBER_UPDATED) {
                        $scope.member = result.entity;
                        sessionStorage.member = angular.toJson($scope.member);
                        $rootScope.profileImageIsLoaded = false;
                        $scope.profileImage = MemberFactory
                                .getPhoto($scope.member.image, $scope.session.sessionId);
                    }
                },
                error: function() {

                }
            };
            MemberFactory.setPhoto(params);
        });

    }

]);