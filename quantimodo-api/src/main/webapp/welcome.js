/**
 * 
 */
function welcomeCtrl($scope, $http, $timeout) {

	$scope.identity = "anonymous";
	$scope.userID = 0;

	$scope.offset = 0;
	$scope.length = 2;
	$scope.variableId = 1135;
	$scope.startTime = 0;
	$scope.endTime = new Date().getTime();

	$scope.behaviour = "output";
	$scope.categoryID = 1;
	
	$scope.varType = "AGGREGATED";
	$scope.fromDate = "Jul 2, 2013";
	$scope.toDate = "";
	$scope.grouping = 4;
	
	$http.get("/user_id/").success(onUserSuccess).error(onUserError);

	function onUserSuccess(data, status, headers, config) {

		if (data == "none") {
			$scope.identity = "anonymous";
			$scope.userID = 0;
		} else {
			$scope.identity = "authenticated";
			$scope.userID = data;
		}
	}

	function onUserError(data, status, headers, config) {
	}

}