/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

module.exports = function (config) {
  config.set({
    basePath: './',

    files: [
      '../../main/resources/app/lib/angular/angular.js',
      '../../main/resources/app/lib/angular/angular-translate.js',
      '../../main/resources/app/lib/angular/angular-translate-loader-static-files.js',
      '../../main/resources/app/lib/angular/angular-route.js',
      '../../main/resources/app/lib/angular/angular-resource.js',
      '../../main/resources/app/lib/angular/angular-*.js',
      '../../main/resources/app/js/**/*.js',
      'test/lib/angular/angular-mocks.js',
      'test/unit/**/*.js'
    ],

    exclude: [
      'app/lib/angular/angular-loader.js',
      'app/lib/angular/*.min.js',
      'app/lib/angular/angular-scenario.js'
    ],

    autoWatch: true,

    frameworks: ['jasmine'],

    browsers: ['Chrome'],

    plugins: [
      'karma-junit-reporter',
      'karma-chrome-launcher',
      'karma-phantomjs-launcher',
      'karma-coverage',
      'karma-firefox-launcher',
      'karma-jasmine'
    ],

    junitReporter: {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    },

    preprocessors: {
      'app/js/*.js': 'coverage'
    },

    reporters: ['coverage'],

    coverageReporter: {
      type: 'html',
      dir: 'coverage/'
    }

  })
}