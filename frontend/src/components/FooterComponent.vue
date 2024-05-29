<script setup lang="ts">
import { ref } from 'vue';

const version = ref(null);

fetch(`./assets/version.txt`)
  .then(res => res.text())
  .then(v => {
    version.value = v && v.length < 30 ? `v${v}` : null;
  })
  .catch(() => {});
</script>

<template>
  <footer class="footer wrapper">
    <nav>
      <ol>
        <li v-show="version">{{ version }}</li>
        <li><a href="https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service" target="_blank">GitLab</a></li>
        <li>
          <a href="https://nfdi4culture.de/services/details/annotation-terminology-lookup-and-personalization-antelope.html" target="_blank"
            >NFDI4Culture</a
          >
        </li>
        <li><a href="https://www.tib.eu/en/imprint" target="_blank">Contact</a></li>
      </ol>
    </nav>
    <div class="footer-org">
      <a href="mailto:antelope@tib.eu">antelope@tib.eu</a>
      &copy; {{ new Date().getFullYear() }}
      <a href="https://www.tib.eu/en/research-development/research-groups-and-labs/open-science/research-development" target="_blank"
        >TIB | Open Science Lab</a
      >
    </div>
  </footer>
</template>

<style scoped lang="scss">
@import '@/assets/scss/shared';

.footer {
  position: relative;
  margin-top: calc(1.5 * var(--space-l));
  padding-top: var(--space-s);
  padding-bottom: var(--space-m);
  //background-color: var(--color-bg-gray);
  font-weight: 200;
  border-top: 1px solid var(--color-bg-contrast);

  @include nav;
  @include s {
    flex-direction: column;
  }

  /* &::before {
			content: "";
			position: absolute;
			top: 0;
			left: -100vw;
			width: 300vw;
			height: 100%;
			background-color: inherit;
			z-index: -1;
		} */

  nav {
    @include s {
      margin-bottom: var(--space-s);
      max-width: $wrapperWidth;
    }
  }

  &-org {
    &::after {
      display: none !important;
    }

    a {
      margin-right: var(--space-s);

      &:hover {
        color: var(--color-secondary);
      }
    }
  }
}
</style>
