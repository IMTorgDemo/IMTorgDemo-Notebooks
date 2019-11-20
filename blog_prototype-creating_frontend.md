
+++
title = "Prototyping an Interactive Frontend with Vue"
date = "2019-08-29"
author = "Jason Beach"
categories = ["Frontend", "category2"]
tags = ["vue", "JavaScript", "tag3"]
+++ 


Assumes you are comfortable with HTML, CSS, JS.  This will cover creating SPAs with webpack and Vue basics.  This will not cover mobile or advanced Vue.  Improvements will make frontend development a more imperative programming approach as compared to the typical declarative methods found in more basic work.










## Introduction

Once you understand the basics of browser languages, such as HTML, CSS, and JS, then you quickly desire to have more interactive elements.  The obvious evolution is jQuery, as you can directly perform imperative programming to manipulate elements.  That was a powerful web technology for about a decade, and it is still used today.  But, frontend programmers quickly learned that jQuery code became extremely crowded, untestable, and overly-complex.  There were limits to what could be practically built with jQuery.  The upper limit was Single Page Applications (SPAs).

SPAs allow for much more dynamic and intuitive interfaces for users.  However, to build and maintain them, more powerful technologies needed to perform scoped logic on modular components.  The first two frameworks to support this were Angular and React.  Vue came slightly later and was able to capitalize on some of the mistakes learned from the first two.  This allowed for a powerful system and fast workflow.

This post will explain how to prototype dynamic user interfaces as well as provide an understanding of more advanced technologies used by frontend programmers. 










## Webpack and Modular Programming

* [ref: basic concepts](https://webpack.js.org/concepts)
* [ref: modules](https://webpack.js.org/concepts/modules)
* [ref: getting started](https://webpack.js.org/guides/getting-started)

Webpack builds a dependency graph and uses the graph to generate an optimized bundle where scripts will be executed in the correct order.  In a more opinionated manner, it bundles javascript applications that are built using a modular design.  Modular programming is used so that verification, debugging, and testing can be performed as an independent and isolated unit.  They can then be added to other modules using a simple ES2015 `import` or CommonJs `require` statement.  

There are a variety of different flavors of JS used for frontend development, including [Coffescript](https://coffeescript.org/#overview), [TypeScript](https://www.typescriptlang.org/) and [ESNext (Babel)](https://babeljs.io/).  These can be compiled into JS, then used in browsers.  Webpack supports modules written in a variety of languages and preprocessors, via loaders. Loaders describe to webpack how to process non-JavaScript modules and include these dependencies into your bundles. 

There are a few concepts to know about how webpack works

* Entry - module to begin dependency graph (default: ./src/index.js)
* Output - where to emit bundles (default: ./dist/main.js)
* Loaders - default only JavaScript and JSON files. allow webpack to process, convert other files into valid modules
* Plugins - wider range of tasks like bundle optimization, asset management and injection of environment variables
* Mode - set to: development, production or none; enables built-in optimizations (default: production)
* Browser Compatibility - supports all browsers that are ES5-compliant 



### Simple usage 

i. Install

```bash
#start project
npm init
#for development only
npm install webpack --save-dev
#instead of using `file:///` url
npm install webpack-dev-server --save-dev
```

ii. Configure

Used like: `webpack <entry_point> <output_file>`

```js
//package.json
"scripts":{
	"build": "webpack-dev-server --entry ./src/js/app.js --output-filename ./dist/bundle.js"
	"build-prod": "webpack /src/js/app.js dist/bundle.js -p"	#minify for production
}
```

iii. Implement

What dependencies are determined by these only es6 syntax.

```js
//app.js
import {name} from './domloader.js'
```

```js
//domloader.js
export var name = <function>
```

```html
//index.html
<script src="dist/bundle.js">
```


### Simple configuration

* [ref: configuration](https://webpack.js.org/configuration/)

```js
//package.json
"scripts": {
  "build": "webpack --config prod.config.js"
}
```

```js
//webpack.config.js
const path = require('path');

module.exports = {
  entry: './src/index.js',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'dist')
  }
};
```


### Development

* [ref: plugins](https://webpack.js.org/plugins/)

Plugins can support your workflow.  These are three useful plugins:

* HtmlWebpackPlugin - generates new names in output
* clean-webpack-plugin - clean /dist folder before each output

Automatically update webpage
```bash
npm start
```

```js
//package.json
  {
    "name": "development",
    "version": "1.0.0",
    "description": "",
    "private": true,
    "scripts": {
      "test": "echo \"Error: no test specified\" && exit 1",
      "watch": "webpack --watch",
      "start": "webpack-dev-server --open",
      "build": "webpack"
    },
    "keywords": [],
    "author": "",
    "license": "ISC",
    "devDependencies": {
```

```js
//webpack.config.js
  const path = require('path');
  const HtmlWebpackPlugin = require('html-webpack-plugin');
  const { CleanWebpackPlugin } = require('clean-webpack-plugin');

  module.exports = {
    mode: 'development',
    entry: {
      app: './src/index.js',
      print: './src/print.js'
    },
    devtool: 'inline-source-map',
    devServer: {
      contentBase: './dist'
    },
    plugins: [
      // new CleanWebpackPlugin(['dist/*']) for < v2 versions of CleanWebpackPlugin
      new CleanWebpackPlugin(),
      new HtmlWebpackPlugin({
        title: 'Development'
      })
    ],
    output: {
      filename: '[name].bundle.js',
      path: path.resolve(__dirname, 'dist')
    }
  };
```



### Advanced Topics

* [code splitting and bundle analysis](https://webpack.js.org/guides/code-splitting/)
* [env variables](https://webpack.js.org/guides/environment-variables/)
* [progressive web apps](https://webpack.js.org/guides/progressive-web-application/)










## Vue Ecosystem


_Tooling_

* [Vue CLI](https://cli.vuejs.org/) - interactive project scaffolding with zero config rapid prototyping, a runtime dependency (@vue/cli-service) and a full graphical user interface to create and manage Vue.js projects
  - vue-cli-service - built on top of webpack that loads other plugins
  - @vue/cli-plugin- (for built-in plugins)
  - vue-cli-plugin- (for community plugins)
* [Vue (Webpack) Loader](https://medium.com/@nupoor_neha/starting-with-webpack-4-and-vuejs-a-quick-start-tutorial-4a244410d55e) - allows you to author Vue components in a format called Single-File Components (SFCs)
* [Vue (browser) dev-tools Extension](https://github.com/vuejs/vue-devtools) - for in-browser development

_Libraries_

* Testing: [Vue-Test-Utils](https://vue-test-utils.vuejs.org/guides/#getting-started)
* HTTP Client: Axios - requests for consuming API
* Router - official router support
* State Management: Vuex - an architecture for Vue applications and allows simple management of the application state.
* Pre-Render, Production: [NuxtJs](https://nuxtjs.org/) - opinionated for spa

_Component Search_

* [Bit](https://bit.dev/components?q=carousel)
* [curated Vue](https://curated.vuejs.org/)
* [Vue components](https://vuecomponents.com/)

_Dev Env_

* [Storybook](https://www.learnstorybook.com/vue/en/get-started) - build UIs like legos
* [OverVue](https://www.overvue.io/) - prototype-based dev


* [ref: awesome Vue](https://github.com/vuejs/awesome-vue#mobile)



### Vue CLI Scaffolding

* [ref: blog post](https://code.tutsplus.com/tutorials/boost-your-vuejs-workflow-with-vue-cli-3--cms-32232)
* [ref: guide docs](https://cli.vuejs.org/guide/)

Projects created by Vue CLI are pre-configured with most of the common development needs working out of the box, including Vue Loader.


```bash
#single component
npm install -g @vue/cli-service-global
vue serve
(browser)localhost:8080
```

```bash
#project
npm install -g @vue/cli
vue --version
vue create my-project
npm run serve
(browser)localhost:8080
#or with ui for dependency management
vue ui
#or directly access the binary
npx vue-cli-service serve
```


__Project Structure__

* node_modules folder contains the packages that the app and development tools require.
* public folder contains static project assets, which will not be included in the bundling process.
* src folder contains the Vue.js application with all resources.
  - assets folder is used for static resources required by the app, which will be included in the bundling process.
  - components folder is used for the application's components.
  - views folder is used for components which will be displayed using the URL routing feature.
    - App.vue is the root component.
  - main.js is the JavaScript file that creates the Vue instance object.
  - router.js is used to configure the Vue router.
  - store.js is used to configure the data store created with Vuex.
* .gitignore contains a list of files and folders that are excluded from the Git version control.
* babel.config.js contains the configuration settings for the Babel compiler.
* package.json contains a list of the packages required for Vue.js development as well as the commands used for the development tools.
* package-lock.json contains a complete list of the packages required by the project and their dependencies.
* README.md contains general information about the project.

```bash
#install plugin
vue add bootstrap-vue
```

`src/plugins/<new_plugin>`

```bash
#build production bundle of: app, library, or component
npm run build --modern --target app
```

`dist` folder created and two version with the browser-appropriate version selected automatically


```bash
#analyze the build
npm run inspect
```


TODO: Guide > Development










## Vue Basics


Vue.js allows you to simply bind your data models to the representation layer. It also allows you to easily reuse components throughout the application.


### Useful Frontend Terms

* Declarative Views: These are the Views that provide a way of direct data binding between plain JavaScript data models and the representation.
* Directives: These are special HTML elements attributes prefixed with `v-` in Vue.js that allow data binding in different ways.  They apply special reactive behavior to the rendered DOM.
* Reactivity: In the Web, this is actually the immediate propagation of any changes of data to the View layer.


### Vue Foundations

```html
<div id="app">
  {{ message }}
</div>
```

```js
var app = new Vue({
  el: '#app',
  data: {
    message: 'Hello Vue!'
  }
})
```


### Directives

vue directive syntax: 
`<div v-directive:argument of [dynamic argument].<event_modifier or key_modifier>=" js expression | filter ">`

```js
* data output
 - {{}}
	<div>{{ interpolated_data }}</div>
 - v-bind <div v-bind:id="dynamicId"></div> or ':id'
    :[attribName / null]="var"
    :id="elemId"
    :disabled="null"
    :href="url"
 - v-once
 - v-html
	<p>Using v-html directive: <span v-html="rawHtml"></span></p>
* data input
 - v-model
* conditionals
 - v-if
 - v-else-if
 - v-else
 - v-show
 - v-for="(item, index) in items"> 
   v-for="(value, name, index) in object">
   v-for="todo in todos" v-if="!todo.isComplete">
* event listener
 - v-on or '@click'
    :[eventName] 
    :keyup.enter
    :keyup.page-down
    :click
    :click="say('what')">
    :click.once 
    :hover
    :focus
    :submit
* style
 - v-bind:class="{ active: isActive }"></div>
 - v-bind:style="[base, { color: activeColor, fontSize: fontSize + 'px' }]"></div>
```

### Data

instance properties (prefixed by `$`) are different from user-defined (var data={} ) data





### Implementation designs

All js in HTML
```.html
<script>
var vm = new Vue({
  el: '#example',
  data: {},
  props: ['item'],
  methods: {
    name: function(){
      return this.msg
    },
  computed:
  <hooks:created, mounted, updated, destroyed>: function(){} 
}) 
</script>
```

Split js into a globally-registered component
```js
<div id='timer'>
<Timer title='my timer'></Timer>
</div>

Vue.component('Timer',{
	template: '<div>...</div>',
	props: ['title'],			//data passed to and maintained by instance
	data: function () {			//data must be a function to maintain indep data copy
		return {count: 0}
	},
	methods:
})

new Vue({
  el: "#timer",
});

```

Use local registration of sub-component
```js
//ComponentA.vue

```

```js
//ComponentB.vue
import ComponentA from './ComponentA'
export default {
  components: {
    ComponentA
  }
}
```

```js
//main.js
import Vue from 'vue'
import upperFirst from 'lodash/upperFirst'

```

```html
//index.html

```


Single-file component separation-of-layers
```js
//comp.vue
<template>
  <div>This will be pre-compiled</div>
</template>
<script src="./my-component.js"></script>
<style src="./my-component.css"></style>
```

Typical single-file component

* [todo app](https://codesandbox.io/s/o29j95wx9)

This is advanced javascript and must understand

* [npm intro ](https://www.sitepoint.com/beginners-guide-node-package-manager/)
* [npm, up to uninstalling global packages](https://docs.npmjs.com/uninstalling-packages-and-dependencies)
* [learn es2015](https://babeljs.io/docs/en/learn)


```html
#index.html
<div id="app"></div>
```

```js
//index.js
import Vue from 'vue'
import App from './App'

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  template: '<App/>',
  components: { App }
})
```

```js
//App.vue
<template>
	<div id="app">
		<h1>My Todo App!</h1>
		<TodoList/>
	</div>
</template
<script>
import TodoList from './components/TodoList.vue'

export default {
	components: {
		TodoList
	}
}
</script>
<style></style>
```

Unit-testing individual components

* [ref: unit tests](https://vuejs.org/v2/guide/unit-testing.html)

```js
//Component.vue
<template>
  <p>{{ msg }}</p>
</template>

<script>
  export default {
    props: ['msg']
  }
</script>
```

```js
//test.js
import Vue from 'vue'
import MyComponent from './MyComponent.vue'

// helper function that mounts and returns the rendered text
function getRenderedText (Component, propsData) {
  const Constructor = Vue.extend(Component)
  const vm = new Constructor({ propsData: propsData }).$mount()
  return vm.$el.textContent
}

describe('MyComponent', () => {
  it('renders correctly with different props', () => {
    expect(getRenderedText(MyComponent, {
      msg: 'Hello'
    })).toBe('Hello')

    expect(getRenderedText(MyComponent, {
      msg: 'Bye'
    })).toBe('Bye')
  })
})
```


TODO
* [emit](https://www.telerik.com/blogs/how-to-emit-data-in-vue-beyond-the-vuejs-documentation)
* [client-side storage](https://vuejs.org/v2/cookbook/client-side-storage.html)
* [storage with indexdDb](https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API)
* [debugging in vscode](https://vuejs.org/v2/cookbook/debugging-in-vscode.html)








END