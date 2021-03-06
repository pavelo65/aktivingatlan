'use strict';

angular.module('aktivingatlanApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('listing', {
                parent: 'site',
                url: '/listing',
                data: {
                    authorities: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/listing/listing.html',
                        controller: 'ListingController'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                        $translatePartialLoader.addPart('listing');
                        return $translate.refresh();
                    }]
                }
            });
    });
