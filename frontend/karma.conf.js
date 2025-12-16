process.env.CHROME_BIN = process.env.CHROMIUM_BIN || '/usr/bin/chromium';

module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-junit-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {
      },
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    reporters: ['progress', 'junit'],
    coverageReporter: {
      dir: require('path').join(__dirname, 'coverage'),
      subdir: '.',
      reporters: [
        { type: 'lcovonly' },
        { type: 'text-summary' }
      ]
    },
    junitReporter: {
      outputDir: 'test-results/junit', // results will be saved as $outputDir/$browserName.xml
      outputFile: 'TEST-frontend.xml', // if included, results will be saved as $outputDir/$browserName/$outputFile
      useBrowserName: false // add browser name to report and classes names
    },
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    browsers: ['ChromiumHeadlessCI'],
    singleRun: true,
    autoWatch: false,
    restartOnFileChange: false,
    customLaunchers: {
      ChromiumHeadlessCI: {
          base: 'ChromeHeadless',
          flags: [
            '--no-sandbox',
            '--disable-gpu',
            '--disable-dev-shm-usage',
            '--remote-debugging-port=9222'
          ]
      }
    }
  });
};
