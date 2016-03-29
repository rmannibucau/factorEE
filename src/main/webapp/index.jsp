<%@ page import="com.github.rmannibucau.javaeefactory.setup.AnalyticsConfiguration" %>
<%@ page import="javax.inject.Inject" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%!
    private boolean isProd;
    private String root;

    @Inject
    private AnalyticsConfiguration analytics;

    public void jspInit() {
        root = getServletConfig().getServletContext().getContextPath();
        isProd = !"dev".equalsIgnoreCase(System.getProperty("javaeefactory.environment"));
    }
%>
<!DOCTYPE html>
<html>

<head>
    <title>JavaEE Factory</title>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="A Project Factory for JavaEE"/>
    <meta name="keywords" content="java, javaee, tomee, openejb, jaxrs"/>
    <meta name="theme-color" content="#ffffff">

    <link rel="apple-touch-icon" sizes="57x57" href="<%= root %>/favicon/apple-icon-57x57.png">
    <link rel="apple-touch-icon" sizes="60x60" href="<%= root %>/favicon/apple-icon-60x60.png">
    <link rel="apple-touch-icon" sizes="72x72" href="<%= root %>/favicon/apple-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="76x76" href="<%= root %>/favicon/apple-icon-76x76.png">
    <link rel="apple-touch-icon" sizes="114x114" href="<%= root %>/favicon/apple-icon-114x114.png">
    <link rel="apple-touch-icon" sizes="120x120" href="<%= root %>/favicon/apple-icon-120x120.png">
    <link rel="apple-touch-icon" sizes="144x144" href="<%= root %>/favicon/apple-icon-144x144.png">
    <link rel="apple-touch-icon" sizes="152x152" href="<%= root %>/favicon/apple-icon-152x152.png">
    <link rel="apple-touch-icon" sizes="180x180" href="<%= root %>/favicon/apple-icon-180x180.png">
    <link rel="icon" type="image/png" sizes="192x192"  href="<%= root %>/favicon/android-icon-192x192.png">
    <link rel="icon" type="image/png" sizes="32x32" href="<%= root %>/favicon/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="96x96" href="<%= root %>/favicon/favicon-96x96.png">
    <link rel="icon" type="image/png" sizes="16x16" href="<%= root %>/favicon/favicon-16x16.png">
    <link rel="manifest" href="<%= root %>/favicon/manifest.json">
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="msapplication-TileImage" content="<%= root %>/favicon/ms-icon-144x144.png">

    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <link href="<%= root %>/theme/startbootstrap-scrolling-nav-1.0.4/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= root %>/theme/fontawesome/css/font-awesome.min.css" rel="stylesheet">
    <link href='//fonts.googleapis.com/css?family=Ubuntu' rel='stylesheet' type='text/css'>
    <link href="<%= root %>/css/javaee-factory<% if (isProd) { %>.min<% } %>.css" rel="stylesheet">
</head>

<body>

<div id="content">
    <div class="center-block spinner-text">JavaEE Factory is loading...</div>
    <div class="spinner center-block">
        <div class="bounce1"></div>
        <div class="bounce2"></div>
        <div class="bounce3"></div>
    </div>
</div>

<script>
    window.JavaEEFactory = {root: '<%= root %>'};
    <% if (analytics.getCode() != null ) { %>
    window.JavaEEFactory.analytics = '<%= analytics.getCode() %>';
    <% } %>
</script>
<script src="<%= root %>/js/lib/requirejs/require.min.js"></script>
<script>
    define('Constants', [], function () {
        return window.JavaEEFactory;
    });

    <% if (isProd) { // fake jade module since all is already loaded %>
    require.config({
        baseUrl: window.JavaEEFactory.root + '/js/app',
        paths: {
            'jquery': '../../theme/startbootstrap-scrolling-nav-1.0.4/js/jquery',
            'bootstrap': '../../theme/startbootstrap-scrolling-nav-1.0.4/js/bootstrap.min'
        },
        shim: {
            'VueRouter': ['Vue'],
            'VueResource': ['Vue'],
            'jquery-cookie': ['jquery'],
            'typeahead': ['jquery'],
            'bloodhound': ['jquery'],
            'bootstrap': ['jquery'],
            'boostrapNotify': ['bootstrap', 'jquery']
        }
    });
    define('main', ['jquery', 'bootstrap', 'javaeefactory.min'], function () {
        require(['boot']);
    });
    require(['main']);
    <% } else { %>
    require.config({
        baseUrl: window.JavaEEFactory.root + '/js/app',
        paths: {
            'text': '../lib/requirejs/text',
            'Vue': '../lib/vue/vue.min',
            'VueRouter': '../lib/vue/vue-router.min',
            'VueResource': '../lib/vue/vue-resource.min',
            'jquery': '../../theme/startbootstrap-scrolling-nav-1.0.4/js/jquery',
            'bootstrap': '../../theme/startbootstrap-scrolling-nav-1.0.4/js/bootstrap.min',
            'boostrapNotify': '../lib/bootstrap/bootstrap-notify.min',
            'jquery-cookie': '../lib/jquery/js.cookie',
            'bloodhound': '../lib/jquery/bloodhound.min',
            'typeahead': '../lib/jquery/typeahead.jquery.min',
            'FileSaver': '../lib/other/FileSaver.min'
        },
        shim: {
            'VueRouter': ['Vue'],
            'VueResource': ['Vue'],
            'jquery-cookie': ['jquery'],
            'bloodhound': ['jquery'],
            'typeahead': ['jquery'],
            'bootstrap': ['jquery'],
            'boostrapNotify': ['bootstrap', 'jquery']
        },
        waitSeconds: 0 // no timeout during tests
    });

    // useful for test/dev
    window.JavaEEFactoryTest = {ajax: 0};
    define('test', ['init', 'Vue'], function (init, Vue) {
        Vue.http.interceptors.push(function () {
            return {
                request: function (request) {
                    window.JavaEEFactoryTest.ajax++;
                    return request;
                },

                response: function (response) {
                    window.JavaEEFactoryTest.ajax--;
                    return response;
                }
            };
        });
    });

    require(['bootstrap', 'test', 'boot']);
    <% } %>
</script>

</body>

</html>
