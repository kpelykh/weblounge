'use strict';

// Declare app level module which depends on filters, and services
angular.module('webledit', [
  'ngRoute',
  'webledit-resources'
])

// Configure the navigation routes
.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider, ResourcesBrowserCtrl) {
    $routeProvider.when('/resources', {templateUrl: 'app/resources/browse.tmpl.html', controller: 'ResourcesBrowserCtrl'});
    $routeProvider.otherwise({redirectTo: '/resources'});
  
    //$locationProvider.html5Mode(false);
  }
])

.config(['RestangularProvider', function(RestangularProvider) {
  RestangularProvider.setRequestSuffix('.json');
}]);