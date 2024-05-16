import "./assets/scss/main.scss";

import { createApp } from "vue";
import { createRouter, createWebHistory, type Router } from "vue-router";

import App from "./App.vue";
import ErrorView from "./views/ErrorView.vue";
import HomeView from "./views/HomeView/HomeView.vue";

const router: Router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		{
			path: "/",
			name: "home",
			component: HomeView,
		},
		{
			path: "/:pathMatch(.*)*",
			name: "not-found",
			component: ErrorView,
		},
	],
});

createApp(App).use(router).mount("#app");
