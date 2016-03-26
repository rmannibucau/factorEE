import Vue from "Vue";
import template from "text!template/factory/generator.html";
import FactoryService from "service/factory.service";
import notifier from "util/notification";
import $ from 'jquery';
import typeahead  from 'typeahead';
import 'FileSaver';

export default Vue.extend({
    template,
    data() {
        return {
            project: {
                buildType: '',
                version: '0.0.1-SNAPSHOT',
                group: 'com.company',
                artifact: 'application',
                name: 'An Application',
                description: 'A generated application',
                packageBase: 'com.company.application',
                packaging: 'war',
                javaVersion: '1.8',
                facets: []
            },
            view: {
                light: true
            },
            facetPlaceholder: ''
        };
    },
    methods: {
        fetchConfiguration() {
            FactoryService.getConfiguration(
                configuration => {
                    this.$set('configuration', configuration);
                    this.facetPlaceholder = Object.keys(configuration.facets).join(', ');

                    // set this once config is loaded to be able to match one option
                    this.project.buildType = 'Maven';
                    this.project.javaVersion = '1.8';

                    // completion
                    $('#projectFacetSearch').typeahead({
                        source: Object.keys(configuration.facets),
                        afterSelect: item => {
                            this.project.facets.push(item);
                            $('#projectFacetSearch').val('');
                        },
                        highlighter: function (item) {
                            var description = configuration.facets[item];
                            var html = '<div><strong>' + item + '</strong><br/><small>' + description + '</small></div>';
                            return html;
                        },
                    });
                },
                error => notifier.error('Error', 'Can\'t retrieve factory configuration (HTTP ' + error.status + ').'));
        },
        zip() {
            // ensure javaVersion is a string otherwise it will likely be converted to a number and can fail on server side
            this.project.javaVersion = '' + this.project.javaVersion;

            FactoryService.generate(
                this.project,
                stream => window.saveAs(new Blob([stream], {type: 'application/zip'}), (this.project.artifact || 'example') + '-' + (this.project.version || '0') + '.zip'),
                () => notifier.error('Error', 'Can\'t create the project due to a server error.'));
        },
        removeFacet(idx) {
            this.project.facets.splice(idx, 1);
        },
        showAll() {
            this.view.light = false;
        },
        showLight() {
            this.view.light = true;
        }
    },
    ready() {
        this.fetchConfiguration();
    },
    route: {
        canReuse() {
            return false;
        }
    }
});
