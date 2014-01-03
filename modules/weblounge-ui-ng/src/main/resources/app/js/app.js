'use strict';


// Declare app level module which depends on filters, and services
angular.module('adminNg', [
    'pascalprecht.translate',
    'ngRoute',
    'adminNg.controllers',
    'adminNg.metadataServices',
    'adminNg.languageServices',
    'adminNg.directives'
]).config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/simple-demo', {templateUrl: 'partials/simple-demo.html', controller: 'MetadataController'});
    $routeProvider.when('/translation-demo', {templateUrl: 'partials/translation-demo.html', controller: 'LangCtrl'});
    $routeProvider.otherwise({redirectTo: '/translation-demo.html'});
}])
.config(['$translateProvider', function ($translateProvider) {
    var options = {
        'prefix': 'org/opencastproject/adminui/languages/lang-',
        'suffix': '.json',
        'translateProvider': $translateProvider
    };
    $translateProvider.useLoader('customLanguageLoader', options);
    $translateProvider.preferredLanguage('en_US'); // This triggers the configuration process of our custom loader
}]);
