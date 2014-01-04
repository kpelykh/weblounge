'use strict';

angular.module('webledit-resources', [
  'restangular'
])

.controller('ResourcesBrowserCtrl', ['$scope', 'Restangular', function ($scope, Restangular) {
  var Resource = Restangular.all('resources');
  var allResources = Resource.getList();
  
  $scope.resources = allResources;
}]);