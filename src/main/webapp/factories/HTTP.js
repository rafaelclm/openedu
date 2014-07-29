openedu.factory('HTTP', [ '$http', function HTTP($http) {

	var HTTP = {};

	HTTP.get = function(params) {

		$http({
			url : params.service,
			method : 'GET',
			responseType : 'json',
			params : params.data
		}).success(function(result) {
			params.success(result);
		}).error(function() {
			// params.error();
		});

	};

	HTTP.post = function(params) {

		$http({
			url : params.service,
			method : 'POST',
			responseType : 'json',
			data : params.data,
                        headers: params.headers
		}).success(function(result) {
			params.success(result);
		}).error(function() {
			// params.error();
		});

	};

	return HTTP;

} ]);