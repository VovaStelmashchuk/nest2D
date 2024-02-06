import {createRouter, createWebHistory} from 'vue-router'
import ProjectList from "@/views/ProjectList.vue";
import TermsAndConditions from "@/views/TermsAndConditions.vue";
import BlogView from "@/views/BlogView.vue";
import ProjectView from "@/views/ProjectView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: ProjectList
        },
        {
            path: '/project/:id',
            name: 'ProjectView',
            component: ProjectView,
            props: true
        },
        {
            path: '/terms-and-conditions',
            name: 'terms-and-conditions',
            component: TermsAndConditions
        },
        {
            path: '/blog',
            name: 'blog',
            component: BlogView
        }
    ]
})

export default router
