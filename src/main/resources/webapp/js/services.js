(function () {
    'use strict';

    /* Services */

    var services = angular.module('prevac.services', ['ngResource']);

    services.factory('Screenings', function($resource) {
        return $resource('../prevac/screenings', {}, {
            'get': {url: '../prevac/screenings/:id', method: 'GET'}
        });
    });

    services.factory('Clinics', function($resource) {
        return $resource('../prevac/clinics', {}, {});
    });

    services.factory('ScreenedParticipants', function($resource) {
        return $resource('../prevac/participants/screened', {}, {});
    });

}());
