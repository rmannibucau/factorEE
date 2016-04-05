import Vue from "Vue";
import template from "text!template/factory/generator.html";
import FactoryService from "service/factory.service";
import notifier from "util/notification";
import $ from 'jquery';
import Constants from "Constants";
import 'typeahead';
import 'bloodhound';

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
            }
        };
    },
    methods: {
        fetchConfiguration() {
            FactoryService.getConfiguration(
                configuration => {
                    this.$set('configuration', configuration);

                    // set this once config is loaded to be able to match one option
                    this.project.buildType = 'Maven';
                    this.project.javaVersion = '1.8';

                    // completion
                    let categories = Object.keys(configuration.facets);
                    let lastCategory = categories ? categories[categories.length - 1] : '';
                    let typeaheadConfig = categories
                        .map(category => {
                            let engine = new Bloodhound({
                                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name', 'description'),
                                queryTokenizer: Bloodhound.tokenizers.whitespace,
                                local: configuration.facets[category],
                                identify(obj) { return obj.name; }
                            });
                            return {
                              name: category,
                              display: 'name',
                              source(q, sync) {
                                return q ? engine.search(q, sync) :  sync(engine.index.all());
                              },
                              templates: {
                                    header: '<h3><i class="fa fa-hashtag"></i> ' + category + '</h3>',
                                    footer: function (context) {
                                        return context.dataset != lastCategory ? '<hr>' : '';
                                    },
                                    suggestion(item) {
                                        return '<div><strong>' + item.name + '</strong><br/><small>' + item.description + '</small></div>';
                                    },
                                    empty(context) {
                                        return '<h3>' + context.dataset + '</h3><div class="tt-suggestion">No Result matching \'' + context.query + '\'</div>';
                                    }
                              }
                            }
                        });
                    typeaheadConfig.unshift({
                        highlight: true,
                        minLength: 0
                    });
                    let input = $('#projectFacetSearch');
                    input.typeahead.apply(input, typeaheadConfig);
                    input.bind('typeahead:select', (evt, item) => {
                        if (this.project.facets.indexOf(item.name) < 0) {
                            this.project.facets.push(item.name);
                        }
                        input.typeahead('val', ''); // clear value
                        input.blur(); // remove focus to get it back when reclicking
                    });
                },
                error => notifier.error('Error', 'Can\'t retrieve factory configuration (HTTP ' + error.status + ').'));
        },
        zip() {
            // ensure javaVersion is a string otherwise it will likely be converted to a number and can fail on server side
            this.project.javaVersion = '' + this.project.javaVersion;

            // create a link and click on it using GET endpoint, FileSaver doesn't work on Safari
            let a = document.createElement('a');
            a.appendChild(document.createTextNode("Download zip"));
            a.style = 'display: none;';
            let link = Constants.root + '/api/factory/zip' +
                '?buildType=' + encodeURIComponent(this.project.buildType) +
                '&version=' + encodeURIComponent(this.project.version) +
                '&group=' + encodeURIComponent(this.project.group) +
                '&artifact=' + encodeURIComponent(this.project.artifact) +
                '&name=' + encodeURIComponent(this.project.name) +
                '&description=' + encodeURIComponent(this.project.description) +
                '&packageBase=' + encodeURIComponent(this.project.packageBase) +
                '&packaging=' + encodeURIComponent(this.project.packaging) +
                '&javaVersion=' + encodeURIComponent(this.project.javaVersion);
            $.each(this.project.facets, (idx, item) => link = link + '&facets=' + encodeURIComponent(item));
            a.href = link;
            a.download = (this.project.artifact || 'example') + '-' + (this.project.version || '0') + '.zip';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
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
