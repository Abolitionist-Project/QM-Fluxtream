/**
 * 
 */
function welcomeCtrl($scope, $http, $timeout) {

	$scope.identity = "anonymous";
	$scope.userId = 0;
	$scope.isUserIdValid = true;
	$scope.userIdCssClass = "";

	$http.get("user_id/").success(onUserSuccess).error(onUserError);

	$scope.logout = function() {
		$http.post("user_id/").success(onUserSuccess).error(onUserError);
	};

	$scope.logoutApi = function() {
		$http.get("api/logout/").success(
				function(data, status, headers, config) {
				}).error(function(data, status, headers, config) {
		});
	};

	$scope.login = function() {

		validateUserId();

		if ($scope.isUserIdValid) {
			$http.post("user_id/", "userId=" + $scope.userId, {
				headers : {
					"Content-Type" : "application/x-www-form-urlencoded"
				}
			}).success(onUserSuccess).error(onUserError);
		}

		return;
	};

	function onUserSuccess(data, status, headers, config) {

		if (data == "none") {
			$scope.identity = "anonymous";
			$scope.userId = 0;
		} else {
			$scope.identity = "authenticated";
			$scope.userId = data;
		}
	}

	function onUserError(data, status, headers, config) {
	}

	function validateUserId() {
		console.log(/^(?:admin)?\d+$/.test($scope.userId));
		if (/^(?:admin)?\d+$/.test($scope.userId)) {
			$scope.isUserIdValid = true;
			$scope.userIdCssClass = "";
		} else {
			$scope.isUserIdValid = false;
			$scope.userIdCssClass = "invalid-input-text";
		}
	}

}
