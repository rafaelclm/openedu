openedu.service('HttpConfig', [function() {

        return {
            HOST: 'http://192.168.0.104',
            PORT: ':8084',
            PATH: '/openedu/rest/',
            getBaseURI: function() {
                return this.HOST + this.PORT + this.PATH;
            }
        };

    }]);