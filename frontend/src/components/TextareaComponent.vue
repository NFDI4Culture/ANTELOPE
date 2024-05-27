<script setup lang="ts">
    import { onMounted, Ref, ref, useAttrs } from "vue";
    
    import { ISearchData } from "../interfaces";
    import { EnvService } from "../services/EnvService";
    import InfoComponent from "./InfoComponent.vue";

    const labelEl: Ref<HTMLLabelElement> = ref(null);
    const textareaEl: Ref<HTMLInputElement> = ref(null);
    const placeholder: Ref<string> = ref(null);
    const monospace = ref(false);
    const emptyLabel = ref(false);
    const text = ref("");

    onMounted(() => {
        placeholder.value = useAttrs().placeholder as string;
        monospace.value = useAttrs().monospace !== undefined;

        emptyLabel.value = !labelEl.value.textContent.trim().length;

        if(EnvService.IS_DEV && !monospace.value) {
            text.value = placeholder.value.replace(/^ *e\.g\./, "").trim();
            console.warn("Placeholder set to textarea as of development mode")
        }
    });

    defineExpose({
        collapse() {
            textareaEl.value.style.height = "0";
        },
        expand() {
            textareaEl.value.style.height = "15.5rem";
        },
        clear() {
            text.value = "";
        },
        getText() {
            return text.value;
        },
        setText(newText: string) {
            text.value = newText;
        }
    });
</script>

<template>
    <div class="search-text">
        <label :class="emptyLabel ? 'empty' : ''" for="search" ref="labelEl">
            <slot name="prompt"></slot>
            <InfoComponent>
                <slot name="info"></slot>
            </InfoComponent>
        </label>
        <textarea v-model="text" ref="textareaEl" :class="(text.length && monospace) ? 'monospace' : ''" maxlength="1000" :placeholder="placeholder"></textarea>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .search-text {
        margin-bottom: var(--space-m);
        text-align: left;
        
        label {
            display: inline-flex;
            flex-direction: row;
            justify-content: space-between;
            align-items: center;
            
            &.empty {
                margin: 0 !important;
            }
        }
        textarea {
            width: 100%;
            min-height: 4rem;
            max-height: 21rem;
            resize: vertical;

            &.monospace {
                color: var(--color-code);
                font-family: monospace;
                font-size: calc(0.975 * var(--font-size-s));
                line-height: 1.5em;
                letter-spacing: -0.125px;
            }
        }
    }
</style>