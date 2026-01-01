import { ApolloClient, InMemoryCache, createHttpLink } from '@apollo/client/core'
import { provideApolloClient } from '@vue/apollo-composable'

import { setContext } from '@apollo/client/link/context'

// HTTP connection to the API
const httpLink = createHttpLink({
    // Connecting to Feed Service locally
    uri: 'http://localhost:8082/graphql',
})

const authLink = setContext((_, { headers }) => {
    // get the authentication token from local storage if it exists
    const token = localStorage.getItem('token');
    // return the headers to the context so httpLink can read them
    return {
        headers: {
            ...headers,
            Authorization: token ? `Bearer ${token}` : "",
        }
    }
});

// Cache implementation
const cache = new InMemoryCache()

// Create the apollo client
const apolloClient = new ApolloClient({
    link: authLink.concat(httpLink),
    cache,
})

export function setupApollo() {
    provideApolloClient(apolloClient)
}

export { apolloClient }
