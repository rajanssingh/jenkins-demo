import hudson.model.*
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials

def fetchCred() {
    credentialsId = 'gh-test-token'
    def credentials = CredentialsProvider.lookupCredentials(
            StandardCredentials.class,
            Jenkins.instance,
            null,
            null
    )

    def selectedCredential = credentials.find { it.id == credentialsId }
    if (!selectedCredential) {
        error("Credential with ID '${credentialsId}' not found")
    }

    def props = creds.getProperties()
    return props
}
