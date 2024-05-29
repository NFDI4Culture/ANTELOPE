import { fileURLToPath } from 'url';
import { join } from 'path';

import { copy } from './dist.mjs';
import { log } from './log.mjs';

const __dirname = fileURLToPath(new URL('.', import.meta.url));

copy(join(__dirname, '../../backend/src/main/resources/swagger/api.yml'), './api.yml');
copy(join(__dirname, '../api.htm'), '../api.htm');

log('Copied API specification');
