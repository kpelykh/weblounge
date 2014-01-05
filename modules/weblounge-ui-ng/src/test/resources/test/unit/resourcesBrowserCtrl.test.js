'use strict';

describe('Test suite for file resourcesBrowserCtrl.test.js', function() {
  var scope;//we'll use this scope in our tests
  
  // Mock module  to allow us to inject our own dependencies
  beforeEach(angular.mock.module('webledit-resources'));
  
  // Mock the controller for the same reason and include $rootScope and $controller
  beforeEach(angular.mock.inject(function($rootScope, $controller) {
    
    //create an empty scope
    scope = $rootScope.$new();
    //declare the controller and inject our empty scope
    $controller('ResourcesBrowserCtrl', {$scope: scope});
  }));
  
  beforeEach(angular.mock.inject(function($RestangularProvider) {
    RestangularProvider.setRequestSuffix('.json');
  }));
  
  it('$scope.resources should be a Restangular list', function() {
    expect(scope.resources.$object.length).toBe(1); 
  });

});