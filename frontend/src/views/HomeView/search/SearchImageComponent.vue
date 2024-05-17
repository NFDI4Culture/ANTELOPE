<script setup lang="ts">
    import { onMounted, Ref, ref } from "vue";
    import * as d3 from "d3";
    
    import { makeDictionary } from "./util";
    import { IImageSearchData, ISearchDictionary } from "../../../interfaces";
    import { EnvService } from "../../../services/EnvService";
    import InfoComponent from "../../../components/InfoComponent.vue";
    import TextareaComponent from "../../../components/TextareaComponent.vue";
    import CheckboxComponent from "../../../components/CheckboxComponent.vue";
    import ButtonComponent from "../../../components/ButtonComponent.vue";

    const currentFile: Ref<File> = ref(null);
    const currentFileURL: Ref<string|ArrayBuffer> = ref(null);
    const fileEl: Ref<HTMLInputElement> = ref(null);

    const tSearchtext: Ref<TextareaComponent> = ref(null);
    const tDictionary: Ref<TextareaComponent> = ref(null);
    const cSource_Iconclass: Ref<CheckboxComponent> = ref(null);
    const cSettings_ClassDuplicates: Ref<CheckboxComponent> = ref(null);

    const exampleDicts = {
        LIST: [ "michelangelo", "van gogh" ],
        SIMPLE: { "artist": [ "michelangelo", "van gogh" ] },
        EXTENDED: { "entity1": { "label": "artist", "patterns": [ "michelangelo", "van gogh" ], "kb_id": "entity1", "kb_url": "entity1_url" } }
    };

    onMounted(() => {
        if(!EnvService.IS_DEV) return;
        fetch(`${document.location.protocol}//${document.location.host}/src/assets/img/landscape.jpg`)
        .then(response => response.blob())
        .then(data => {
            setFile(new File([ data ], "example.jpg", {
                type: "image/jpeg"
            }));
        });
    });

    function setDictExample(exampleObj: unknown) {
        tDictionary.value.setText(JSON.stringify(exampleObj, null, 2));
        tDictionary.value.expand();
    }

    function setFile(file: File) {
        currentFile.value = file;

        const reader = new FileReader();
        reader.onload = () => {
            currentFileURL.value = reader.result;
        };
        reader.readAsDataURL(currentFile.value);
    }

    function readFile(e) {
        setFile(e.target.files[0]);
    }

    function validate(params: IImageSearchData): IImageSearchData {
        if(!params.body.image) throw new RangeError("Image must not be empty");
        
        return params;
    }

    defineExpose({
        clear() {
            tSearchtext.value.clear();
            tDictionary.value.clear();
            tDictionary.value.collapse();
            
            fileEl.value.value = null;
            currentFile.value = null;
            currentFileURL.value = null;

            // Temporary
            d3
            .select("#barchart")
            .selectAll("*")
            .remove();

        },
        getParams(): IImageSearchData {
            const imageSearchData: IImageSearchData = validate({
                endpoint: "/annotation/entitylinking/image",
                searchParams: {
                    model: "ClipClassification"
                },
                body: {
                    image: currentFile.value,
                    text: tSearchtext.value.getText(),
                    dictionary: makeDictionary(tDictionary.value.getText()),
                    threshold: 0.6,
                    language: "en"
                }
            });

            const formData = new FormData();
            formData.append("image", imageSearchData.body.image);
            formData.append("text", imageSearchData.body.text);
            formData.append("dictionary", new Blob([ JSON.stringify(imageSearchData.body.dictionary) ], { type: "application/json" }));
            formData.append("threshold", imageSearchData.body.threshold.toString());
            formData.append("language", imageSearchData.body.language);

            return {
                ...imageSearchData,

                // @ts-ignore
                body: formData,

                mock: [[{"score":0.0012524304,"label":"michelangelo"},{"score":0.9987476,"label":"van gogh"}]]
            };
        }
    });
</script>

<template>
    <h4>
        File Source
        <InfoComponent>
            Choose an existing dictionary or define your own dictionary (also in combination).
        </InfoComponent>
    </h4>
    <input class="search-image-file" type="file" accept="image/*" ref="fileEl" @change="readFile">
    <img class="search-image-preview" :src="currentFileURL">
    <TextareaComponent placeholder="e.g. The expulsion of Heliodorus from the temple" ref="tSearchtext">
        <template #prompt>
            Give an image description (optional)
        </template>
        <template #info>
            Describe the image in natural language to possbily improve the search results.
        </template>
    </TextareaComponent>
    <h4>
        Dictionary
        <InfoComponent>
            Choose an existing dictionary or define your own dictionary (also in combination).
        </InfoComponent>
    </h4>
    <TextareaComponent placeholder="Start with an example below" monospace ref="tDictionary" />
    <div class="search-image-examples">
        <span class="desktop">Apply Example</span>
        <ButtonComponent contrast small @click="setDictExample(exampleDicts.LIST)">Entity List</ButtonComponent>
        <ButtonComponent contrast small @click="setDictExample(exampleDicts.SIMPLE)">Labelled Entity Lists</ButtonComponent>
        <ButtonComponent contrast small @click="setDictExample(exampleDicts.EXTENDED)">Arbitrary</ButtonComponent>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .search-image {
        &-examples {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: flex-start;
            margin-top: calc(-1 * var(--space-s));
            margin-bottom: var(--space-m);
            color: var(--color-fg-gray);
            
            span {
                margin-right: var(--space-s);

                @include font-s;
            }
        }
        &-file {
            width: 100%;

            &::file-selector-button {
                cursor: pointer;
                position: relative;
                display: inline-flex;
                flex-direction: row;
                align-items: center;
                margin-right: var(--space-xs);
                padding: 0.375em 0.75em;
                background-color: var(--color-secondary);
                color: var(--color-fg-light--light);
                font-family: inherit;
                font-weight: 600;
                text-transform: uppercase;
                border: none;
                border-radius: var(--border-radius-s);
                outline: none;
                user-select: none;
                
                @include font-s;

                &:hover {
                    filter: brightness(0.95);
                }
            }
        }
        &-preview {
            max-width: 20rem;
            max-height: 20rem;
            border: 1px solid var(--color-bg-contrast);
            object-fit: cover;

            @include shadow;
        }
    }
</style>