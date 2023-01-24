const { pathsToModuleNameMapper } = require('ts-jest');

const {
  compilerOptions: { paths = {}, baseUrl = './' },
} = require('./tsconfig.json');
const environment = require('./webpack/environment');

<<<<<<< HEAD
module.exports = {
  transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$|dayjs/esm)'],
  resolver: 'jest-preset-angular/build/resolvers/ng-jest-resolver.js',
=======
const esModules = ['d3', 'd3-array', '@angular'].join('|');
const angularModules = [];

module.exports = {
  //Jest only understands CommonJS, by default Jest dosent transform any module inside node modules. So we need explicit inform Jest to look up and transform any module inside node module that ends up not beeing in commonJS.
  transform: { '^.+\\.(ts|js|mjs|html|svg)$': 'jest-preset-angular'},
  transformIgnorePatterns: [  'node_modules/(?!@angular|@ngrx|@ngrx-translate|@ngx-translate|ngx-webstorage|@ngx-loading-bar|@ng-bootstrap|d3|d3-array|rxjs|delaunator|internmap|countup.js|lodash-es|lodash|@fortawesome|robust-predicates|dayjs|jest-runtime)'],
  resolver: 'jest-preset-angular/build/resolvers/ng-jest-resolver.js',
  extensionsToTreatAsEsm: [".ts"], 
>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
  globals: {
    ...environment,
  },
  roots: ['<rootDir>', `<rootDir>/${baseUrl}`],
  modulePaths: [`<rootDir>/${baseUrl}`],
  setupFiles: ['jest-date-mock'],
  cacheDirectory: '<rootDir>/target/jest-cache',
  coverageDirectory: '<rootDir>/target/test-results/',
  moduleNameMapper: pathsToModuleNameMapper(paths, { prefix: `<rootDir>/${baseUrl}/` }),
  reporters: ['default', ['jest-junit', { outputDirectory: '<rootDir>/target/test-results/', outputName: 'TESTS-results-jest.xml' }]],
  testResultsProcessor: 'jest-sonar-reporter',
  testMatch: ['<rootDir>/src/main/webapp/app/**/@(*.)@(spec.ts)'],
  testURL: 'http://localhost/',
};
<<<<<<< HEAD
=======

>>>>>>> 2e5545b139a65403936136a7357967bad22257fa
