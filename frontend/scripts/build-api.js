import { fileURLToPath } from 'url';
import { join } from 'path';
import { readFileSync } from 'fs';

import { load as loadYAML } from 'js-yaml';

import { copy, write } from './dist.mjs';
import { log } from './log.mjs';

const __dirname = fileURLToPath(new URL('.', import.meta.url));

copy(join(__dirname, '../../backend/src/main/resources/swagger/api.yml'), './api.yml');

const API_CONFIG = loadYAML(readFileSync(join(__dirname, '../../backend/src/main/resources/swagger/api.yml')).toString());

API_CONFIG.servers = [
  {
    url: 'https://service.tib.eu/annotation/api',
    description: 'Service API',
  },
];

write('./api.json', JSON.stringify(API_CONFIG, null, 2));

copy(join(__dirname, '../api.htm'), '../api.htm');

log('Copied API specification');
