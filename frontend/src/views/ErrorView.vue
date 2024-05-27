<script setup lang="ts">
	import { RouterLink } from "vue-router";

	import ButtonComponent from "../components/ButtonComponent.vue";
</script>

<template>
	<main>
		<section class="wrapper">
			<h1>Page not found</h1>
			<ButtonComponent>
				<RouterLink to="/">Go to application</RouterLink>
			</ButtonComponent>
		</section>
	</main>
</template>

<style lang="scss" scoped>
	section {
		display: flex;
		flex-direction: column;
		align-items: center;
	}
</style>