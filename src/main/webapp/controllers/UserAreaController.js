openedu.controller('UserAreaController', ['$scope',
    function UserAreaController($scope) {

        this.template = $scope.Templates.USER_AREA_HOME_CONTENT;
        this.sidebar = true;

        this.setTemplate = function(template) {
            this.template = template;
            if (template === $scope.Templates.USER_AREA_PROFILE) {
            }
        };

    }
]);