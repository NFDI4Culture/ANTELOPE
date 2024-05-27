/* eslint-env node */
require("@rushstack/eslint-patch/modern-module-resolution");

module.exports = {
	root: true,
	"extends": [
		"plugin:vue/vue3-essential",
		"eslint:recommended",
		"@vue/eslint-config-typescript"
	],
	parserOptions: {
		ecmaVersion: "latest"
	},
	rules: {
		"@typescript-eslint/no-unused-vars": [
			2,
			{
				"argsIgnorePattern": "^_+$"
			}
		],
		"indent": [
			"error",
			"tab",
			{
				"MemberExpression": 0
			}
		],
		"linebreak-style": [
			2,
			"unix"
		],
		"quotes": [
			2,
			"double"
		],
		"semi": [
			2,
			"always"
		],
		"prefer-arrow-callback": 2,
		"no-mixed-spaces-and-tabs": 0,
		"no-irregular-whitespace": 0,
		"no-control-regex": 0
	}
};
