<script setup lang="ts">
    import { Ref, ref, onMounted } from "vue";

    type TChangeHandler = (newIndex: number) => void;

    const bodyEl = ref(null);
    const tabs: Ref<{
        index: number;
        label: string;
    }[]> = ref([]);
    const activeTabIndex = ref(0);

    const changeHandlers: TChangeHandler[] = [];

    onMounted(() => {
        const bodies: HTMLElement[] = Array.from(bodyEl.value.children);

        bodies.forEach((child: HTMLElement, i: number) => {
            tabs.value.push({
                index: i,
                label: child.getAttribute("tab-label") ?? ""
            });
        });
        bodies[0].classList.add("active");
    });

    function activateTab(newIndex: number) {
        const bodies: HTMLElement[] = Array.from(bodyEl.value.children);

        const index = (newIndex < bodies.length) ? newIndex : 0; 

        bodies[activeTabIndex.value].classList.remove("active");
        bodies[index].classList.add("active");

        activeTabIndex.value = index;
        
        changeHandlers.forEach((callback: TChangeHandler) => callback(activeTabIndex.value));
    }
    
    defineExpose({
        onTabChange(callback: TChangeHandler) {
            changeHandlers.push(callback);
        },
        getIndex(): number {
            return activeTabIndex.value;
        },
        activateTab
    });
</script>

<template>
    <div class="tabs">
        <ol class="tabs-head">
            <li v-for="tab in tabs" :key="tab.label">
                <a :class="(activeTabIndex === tab.index) ? 'active' : ''" @click="activateTab(tab.index)">{{ tab.label }}</a>
            </li>
        </ol>
        <div class="tabs-body" ref="bodyEl">
            <slot></slot>
        </div>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .tabs {
        width: 100%;

        &-head {
            list-style: none;
            align-items: flex-end;
            padding: 0;
            // border-bottom: 1px solid var(--color-fg-contrast);

            @include flex;
            
            li {
                margin-bottom: 0;
                height: fit-content;
                user-select: none;

                a {
                    display: block;
                    padding: var(--space-xs);
                    color: inherit;
                    text-align: center;
                    text-transform: uppercase;
                    border-bottom: 0.45rem solid /* transparent */var(--color-bg-gray);

                    &:hover {
                        border-bottom-color: var(--color-primary-subtle);
                    }

                    &.active {
                        font-weight: 600;
                        border-bottom-color: var(--color-primary);
                    }
                }
            }
        }
        &-body {
            padding-top: var(--space-xs);
            
            &:deep(> *) {
                display: none;

                &.active {
                    display: block;
                }
            }
        }
    }
</style>