var openedu = angular.module('openedu', ['ui.bootstrap', 'ui.mask', 'imageupload', 'sticky']);

openedu.directive('imageonload', function($rootScope) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind('load', function() {

                if (!$rootScope.profileImageIsLoaded) {

                    $('#profileImage').imgLiquid();
                    $rootScope.profileImageIsLoaded = true;

                }

            });
        }
    };
});
