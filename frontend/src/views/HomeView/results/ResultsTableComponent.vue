<script setup lang="ts">
    import { Ref, ref } from "vue";

    import { IEntity, IResults } from "../../../interfaces";
    import { ResultsService } from "../../../services/ResultsService";
    import { EntitySelectionService } from "../../../services/EntitySelectionService";
    import ButtonComponent from "../../../components/ButtonComponent.vue";
    import EntityComponent from "../../../components/EntityComponent.vue";

    const entities: Ref<IEntity[]> = ref([]);

    ResultsService.on("define", (results: IResults) => {
        entities.value = results.entities
        .filter((entity: IEntity, i: number) => !results.entities.slice(0, i).filter((compareEntity: IEntity) => compareEntity.URI === entity.URI).length);
    });

    function select(entity: IEntity) {
        EntitySelectionService.select(entity);
    }
    function view(URI: string) {
        window.open(URI, "_blank").focus();
    }
</script>

<template>
    <table class="results-table">
        <tr v-for="entity of entities" :key="entity.id">
            <td>
                <EntityComponent>
                    <template #label>
                        {{ entity?.label }}
                    </template>
                    <template #id>
                        {{ entity?.id }}
                    </template>
                </EntityComponent>
            </td>
            <td>
                <ButtonComponent small @click="select(entity)">Select</ButtonComponent>
                <ButtonComponent small contrast @click="view(entity.URI)">View</ButtonComponent>
            </td>
        </tr>
    </table>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .results-table {
        table-layout: fixed;
        width: 100%;

        tr {
            display: flex;
            padding: var(--space-xs) 0;
            width: 100%;

            &:not(:last-of-type) {
                border-bottom: 1px solid var(--color-bg-contrast);
            }

            td:first-of-type {
                flex: 1 0 0;
                display: flex;
                flex-direction: row;
                align-items: center;
            }
            td > * {
                max-width: 25em;
            }
        }
    }
</style>