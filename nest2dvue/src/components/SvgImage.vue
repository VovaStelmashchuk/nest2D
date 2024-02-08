<template>
    <div class="svg-image-container">
        <template v-if="imageLink">
            <img :src="imageLink" alt="SVG Image"/>
        </template>
        <template v-else>
            <p>{{ placeholderText }}</p>
        </template>
    </div>
</template>

<script>
export default {
    name: 'SvgImage',
    props: {
        imageLink: {
            type: String,
            default: ''
        },
        placeholderText: {
            type: String,
            default: 'Placeholder text before image is loaded'
        }
    }
}
</script>

<style>
.svg-image-container {
    background: gray;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    position: relative; /* Needed for pseudo-element positioning */
}

.svg-image-container::before {
    content: '';
    display: block;
    padding-top: 100%; /* This makes the height equal to the width */
}

.svg-image-container img, .svg-image-container p {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: fill;
}

.svg-image-container p {
    display: flex;
    justify-content: center; /* Center text horizontally */
    align-items: center; /* Center text vertically */
    font-size: 24px;
    margin: 0; /* Remove any default margin */
    color: white; /* Text color */
    padding: 10px; /* Add padding to ensure text does not touch the borders */
    overflow-wrap: break-word; /* Allow long words to break and wrap to the next line */
    text-align: center; /* Center text alignment */
    white-space: normal; /* Ensure whitespace is handled normally */
}
</style>
