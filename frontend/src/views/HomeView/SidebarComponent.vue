<script setup lang="ts">
    import { Ref, ref, resolveDirective } from "vue";

    import { IEntity, IResults } from "../../interfaces";
    import { EntitySelectionService } from "../../services/EntitySelectionService";
    import InfoComponent from "../../components/InfoComponent.vue";
    import EntityComponent from "../../components/EntityComponent.vue";
    import ButtonComponent from "../../components/ButtonComponent.vue";
import { ResultsService } from "../../services/ResultsService";

    const entities: Ref<IEntity[]> = ref([]);
    const curResultsEntities: Ref<IEntity[]> = ref([]);

    EntitySelectionService.on("select", update);
    EntitySelectionService.on("unselect", update);

    ResultsService.on("define", (results: IResults) => {
        curResultsEntities.value = results.entities ?? [];
    });
    ResultsService.on("clear", () => {
        curResultsEntities.value = [];
    });

    function update() {
        entities.value = EntitySelectionService.getStored();
    }
    function unselect(entity: IEntity) {
        EntitySelectionService.unselect(entity);

        update();
    }
    function unselectAll() {
        EntitySelectionService.unselectAll();

        update();
    }
    
    function downloadFile(fileName: string, fileExtension: string, contents: string): void {
        const downloadAnchor = document.createElement("a");
        downloadAnchor.setAttribute("href", `data:text/${fileExtension.toLowerCase()};charset=utf-8,${
            encodeURIComponent(contents)
        }`);
        downloadAnchor.setAttribute("download", `${fileName}.${fileExtension.toLowerCase()}`);
        document.body.appendChild(downloadAnchor);
        setTimeout(() => {
            downloadAnchor.click();

            downloadAnchor.remove();
        }, 0);
    }

    function exportJSON(entities: IEntity[]) {
        downloadFile(`antelope-${Date.now()}`, "JSON", JSON.stringify(entities));
    }

    function exportCSV(entities: IEntity[]) {
        const filterOrderSerialObj = (obj: { [ key: string ]: any; }) => {
            const serialKeys: string[] = [];
            for(const key in obj) {
                if([ "string", "number", "boolean" ].includes(typeof(obj[key]))) {
                serialKeys.push(key);
                }
            }
            serialKeys.sort();
            const filteredObj: { [ key: string ]: string|number|boolean; } = {};
            for(const key of serialKeys) {
                filteredObj[key] = obj[key];
            }
            return filteredObj;
        };

        downloadFile(`antelope-${Date.now()}`, "CSV", `${
            Object.keys(filterOrderSerialObj(entities[0])).join(",").trim()
        }\n${
            Array.from(entities)
            .map((e: IEntity) => Object.values(filterOrderSerialObj(e)).join(",").trim())
            .join("\n")
        }`.trim());
    }
</script>

<template>
    <section class="sidebar">
        <h3>
            Your Selection
            <InfoComponent>
                Entities can be selected and stored in order to allow for combined exports.
            </InfoComponent>
        </h3>
        <div class="sidebar-selection">
            <ol>
                <li v-for="entity in entities" :key="entity.id" class="sidebar-selection-entity">
                    <EntityComponent>
                        <template #label>
                            {{ entity?.label }}
                        </template>
                        <template #id>
                            {{ entity?.id }}
                        </template>
                    </EntityComponent>
                    <ButtonComponent contrast small @click="unselect(entity)">
                        <span></span>
                    </ButtonComponent>
                </li>
                <li>No entities selected yet</li>
            </ol>
            <ButtonComponent small contrast @click="unselectAll()">Unselect All</ButtonComponent>
        </div>
        <h4>Export Selected Results</h4>
        <div :class="!entities.length ? 'disable' : ''">
            <ButtonComponent @click="exportJSON(entities)">JSON</ButtonComponent>
            <ButtonComponent @click="exportCSV(entities)">CSV</ButtonComponent>
        </div>
        <h4>Export All Results</h4>
        <div :class="!curResultsEntities.length ? 'disable' : ''">
            <ButtonComponent @click="exportJSON(curResultsEntities)">JSON</ButtonComponent>
            <ButtonComponent @click="exportCSV(curResultsEntities)">CSV</ButtonComponent>
        </div>
    </section>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .sidebar {
        display: flex;
        flex-direction: column;
        align-items: flex-start;

        &, & > div {
            width: fit-content;
        }
        & > button {
            align-self: center;
        }

        &-selection {
            ol {
                list-style: none;
                margin: var(--space-s) 0;
                padding: 0;
                
                li {
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    justify-content: space-between;

                    &:first-child:last-child {
                        display: block;
                    }
                    &:last-child {
                        display: none;
                        color: var(--color-fg-gray);
                    }

                    & > *:first-child {
                        margin-right: var(--space-s);
                    }

                    & > * {
                        max-width: 15em;
                    }
                }
            }
            span {
                margin: 0 -0.5em;

                @include icon("dash");
            }
        }
    }
</style>