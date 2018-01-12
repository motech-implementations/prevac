(function () {
    'use strict';

    /* App Module */
    var prevac = angular.module('prevac', ['prevac.controllers', 'prevac.services',
        'prevac.directives', 'motech-dashboard', 'data-services', 'ui.directives']), clinicId, subjectId;

    $.ajax({
        url: '../mds/entities/getEntity/PREVAC Module/Clinic',
        success:  function(data) {
            clinicId = data.id;
        },
        async: false
    });

    $.ajax({
        url: '../mds/entities/getEntity/PREVAC Module/Participant',
        success:  function(data) {
            subjectId = data.id;
        },
        async: false
    });

    $.ajax({
            url: '../prevac/available/prevacTabs',
            success:  function(data) {
                prevac.constant('PREVAC_AVAILABLE_TABS', data);
            },
            async:    false
        });

    prevac.run(function ($rootScope, PREVAC_AVAILABLE_TABS) {
            $rootScope.PREVAC_AVAILABLE_TABS = PREVAC_AVAILABLE_TABS;
        });

    prevac.config(function ($routeProvider, PREVAC_AVAILABLE_TABS) {

        var i, tab;

        for (i = 0; i < PREVAC_AVAILABLE_TABS.length; i = i + 1) {

            tab = PREVAC_AVAILABLE_TABS[i];

            if (tab === "visitLimitation") {
                $routeProvider.when('/prevac/{0}'.format(tab), {redirectTo: '/mds/dataBrowser/' + clinicId + '/prevac'});
            } else if (tab === "subjects") {
                $routeProvider.when('/prevac/{0}'.format(tab), {
                    templateUrl: '../prevac/resources/partials/prevacInstances.html',
                    controller: 'MdsDataBrowserCtrl',
                    resolve: {
                        entityId: function ($route) {
                            $route.current.params.entityId = subjectId;
                        },
                        moduleName: function ($route) {
                            $route.current.params.moduleName = 'prevac';
                        }
                    }
                });
            } else if (tab === "reports") {
                $routeProvider
                    .when('/prevac/{0}'.format(tab),
                        {
                            templateUrl: '../prevac/resources/partials/{0}.html'.format(tab)
                        }
                    )
                    .when('/prevac/reports/capacityReport',
                        {
                            templateUrl: '../prevac/resources/partials/capacityReport.html',
                            controller: 'Prevac{0}Ctrl'.format(tab.capitalize())
                        }
                    );
            } else {
                $routeProvider.when('/prevac/{0}'.format(tab),
                    {
                        templateUrl: '../prevac/resources/partials/{0}.html'.format(tab),
                        controller: 'Prevac{0}Ctrl'.format(tab.capitalize())
                    }
                );
            }
        }

        $routeProvider
            .when('/prevac/settings', {templateUrl: '../prevac/resources/partials/settings.html', controller: 'PrevacSettingsCtrl'})
            .when('/prevac/welcomeTab', { redirectTo: '/prevac/' + PREVAC_AVAILABLE_TABS[0] });

    });
}());
