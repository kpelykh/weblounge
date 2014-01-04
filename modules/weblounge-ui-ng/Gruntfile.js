module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({

        /**===================================
         * Configuration variables
         ====================================*/

        pkg: grunt.file.readJSON('package.json'),

        /** JSHint properties */
        jshintProperties: grunt.file.readJSON('jshint.json'),

        /** The path to the app sources */
        baseDir: "src/main/resources/app",
        testDir: "src/test/resources/app",

        /** The current target file for the watch tasks */
        currentWatchFile: "",

        /** Local directory for the dev server */
        serverDir: "www",

        /** Paths for the different types of resources */
        srcPath: {
            js   : "<%= baseDir %>/**/*.js",
            less : "<%= baseDir %>/css/*.less",
            html : "<%= baseDir %>/**/*.html",
            mocks: "<%= testDir %>/**/*.json",
            www  : "<%= serverDir %>"
        },

        /** The profile being use currently */
        currentProfile: undefined,

        /** The diffferent profiles */
        profiles: {
            local: {
                target: "<%= srcPath.www %>"
            },
            integration: {
                target: "$FELIX_HOME/lib/local/"
            }
        },

        /**===================================
         * Tasks
         ====================================*/

        /** Linter task, use the jshint.json file for the option */
        jshint: {
            one     : '<%= currentWatchFile %>',
            all     : ['<%= srcPath.js %>'],
            options : '<%= jshintProperties %>'
        },

        /** Task to watch src files and process them */
        watch: {
            options: {
                nospawn: true
            },
            // Watch Javascript file
            js: {
                files: ["<%= srcPath.js %>"],
                tasks: ['jshint:one', 'copy:one']
            },
            html: {
                files: ["<%= srcPath.html %>"],
                tasks: ['copy:one']
            },
            mocks: {
               files: ["<%= srcPath.mocks %>"],
               tasks: ['copy:one']
            },
            // Watch less file
            less: {
                files: ["<%= srcPath.less %>"],
                tasks: ['less:css', 'copy:style']
            },
            // Watch file on web server for live reload
            www: {
                options: {
                    livereload: true
                },
                files: ["<%= srcPath.www %>/app/**/*", "<%= srcPath.www %>"]
            }
        },

        /** Compile the less files into a CSS file */
        less: {
            css: {
                options: {
                    paths         : [ "<%= baseDir %>/css/" ],
                    syncImport    : true,
                    strictImports : true,
                    concat        : true,
                    compress      : true,
                },
                files: {
                    "<%= baseDir %>/css/app.css": "<%= baseDir %>/css/app.less"
                }
            }
        },

        /** Copy .. */
        copy: {
            // ... a single file locally
            one: {
                files: [{
                    cwd     : '<%= baseDir %>',
                    flatten : false,
                    expand  : true,
                    src     : '<%= currentWatchFile %>',
                    dest    : '<%= serverDir %>',
                    filter  : 'isFile'
                }]
            },
            // ... the stylesheet locally
            style: {
                files: [{
                    src  : 'style/style.css',
                    dest : '<%= currentProfile.target %>/'
                }]
            },
            // ... all the tool files for the current profile
            all: {
                files   : [{
                    cwd     : '<%= baseDir %>',
                    flatten : false,
                    expand  : true,
                    src     : ['**/*', '!./.*'],
                    dest    : '<%= currentProfile.target %>'
                }, {
                    cwd     : '<%= testDir %>',
                    flatten : false,
                    expand  : true,
                    src     : ['**/*', '!./.*'],
                    dest    : '<%= currentProfile.target %>'
                }]
            }
        },

        /** Task to run tasks in parrallel */
        concurrent: {
            dev: {
               tasks: ['watch:js', 'watch:less', 'watch:html', 'watch:mocks', 'connect'],
                options: {
                    logConcurrentOutput: true
                }
            }
        },

        /** Web server */
        connect: {
            server: {
                options: {
                    port       : 9002,
                    base       : '<%= serverDir %>',
                    keepalive  : true,
                    livereload : false
                }
            }
        },

        /** Test runner */
        karma: {
            options: {
                configFile : 'src/test/resources/karma.conf.js',
                runnerPort : 9999
              },
              continuous: {
                singleRun : true,
                browsers  : ['PhantomJS']
              },
              dev: {
                reporters : 'dots',
                browsers  : ['Chrome', 'Firefox']
              }
        },

        /** Documentation generator */
        docular: {
            groups: [],
            showDocularDocs: true,
            showAngularDocs: true
        }
    });

    // Load the plugin that provides the "uglify" task.
    grunt.loadNpmTasks('assemble-less');
    grunt.loadNpmTasks('grunt-concurrent');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-docular');
    grunt.loadNpmTasks('grunt-karma');


    // Base task for the development
    grunt.registerTask('baseDev', ['less:css', 'copy:all', 'concurrent:dev']);
    
    // Task for continuous integration
    grunt.registerTask('ci', ['jshint:all', 'karma:continuous']);

    grunt.registerTask('dev', "Develop task", function (profile) {
        grunt.log.writeln("Develop task with profile '" + profile + "' started!");
        grunt.config.set('currentProfile', grunt.config.get('profiles.' + profile));
        grunt.task.run('baseDev');
    });

    grunt.registerTask('debug', "Debug copying issue", function (profile) {
        grunt.log.writeln("debug with profile '" + profile + "' started!");
        grunt.config.set('currentProfile', grunt.config.get('profiles.' + profile));
        grunt.config.set('currentWatchFile', ['js/app.js']);
        grunt.log.write("I have set currentWatchFile to " +  grunt.config.get('currentWatchFile'));
        grunt.task.run('copy:one');
    });

    // on watch events configure jshint:all to only run on changed file
    grunt.event.on('watch', function(action, filepath, target) {
        grunt.log.writeln(target + ': ' + filepath + ' has ' + action);
        if (target == "js") {
            var ext = filepath.split(".").pop();

            switch (ext) {
                case "js":
                    grunt.config.set('currentWatchFile', [filepath.replace('src/main/resources/app/', '')]);
                    grunt.task.run('jshint:one');
                    break;
                default:
                    grunt.config.set('currentWatchFile', [filepath]);
                    break;
            }
        }
        else if (target === "html") {
            grunt.log.write(filepath + ' has changed');
            grunt.config.set('currentWatchFile', [filepath.replace('src/main/resources/app/', '')]);
        }
        else if (target === "json") {
            grunt.log.write(filepath + ' has changed');
            grunt.config.set('currentWatchFile', [filepath.replace('src/test/resources/', '')]);
        }
        else {
            grunt.config.set('currentWatchFile', [filepath.replace('src/main/resources/', '')]);
        }
           
    });
};
