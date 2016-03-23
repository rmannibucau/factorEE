({
    name: 'boot',
    baseUrl: '${project.build.directory}/frontend/js/app',
    out: '${project.build.directory}/${project.build.finalName}/js/app/javaeefactory.min.js',
    optimize: 'uglify',
    paths: {
        'Vue': '../../../../src/main/webapp/js/lib/vue/vue.min',
        'VueRouter': '../../../../src/main/webapp/js/lib/vue/vue-router.min',
        'VueResource': '../../../../src/main/webapp/js/lib/vue/vue-resource.min',
        'boostrapNotify': '../../../../src/main/webapp/js/lib/bootstrap/bootstrap-notify.min',
        'moment': '../../../../src/main/webapp/js/lib/moment/moment.min',
        'text': '../../../../src/main/webapp/js/lib/requirejs/text',
        'jquery-cookie': '../../../../src/main/webapp/js/lib/jquery/js.cookie',
        'typeahead': '../../../../src/main/webapp/js/lib/bootstrap/bootstrap3-typeahead.min',
        'FileSaver': '../../../../src/main/webapp/js/lib/other/FileSaver.min',
        'jquery': 'empty:',
        'bootstrap': 'empty:',
        'Constants': 'empty:',
    },
    shim: {
        'VueRouter': ['Vue'],
        'VueResource': ['Vue'],
        'jquery-cookie': ['jquery'],
        'bootstrap': ['jquery'],
        'typeahead': ['bootstrap'],
        'boostrapNotify': ['bootstrap', 'jquery']
    }
})
