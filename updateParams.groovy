//def mystring = "['one','two']"
def myparam="$params"
// Remove square brackets and whitespace
def listString = myparam.replaceAll(/\[|\]|\s+/, '')

// Split the string by comma
def elements = listString.split(',')
println elements
// Create ArrayList and add elements
def arrayList = new ArrayList(elements)

printl arrayList
job('example') {
    parameters {
        booleanParam('FLAG', true)
        choiceParam('OPTION',arrayList)
    }
}