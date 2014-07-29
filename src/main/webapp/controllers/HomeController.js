openedu.controller('HomeController', ['$scope', function HomeController($scope) {

        this.model = $scope;
        this.template;

        this.doLogin = function() {
            this.template = this.model.Templates.SIGNIN;
        };

        this.cancel = function() {
            this.template = this.model.Templates.HOME;
        };

        this.doSignUp = function() {
            this.template = this.model.Templates.SIGNUP;
        };

    }

]);
