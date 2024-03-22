//def mystring = "'one','two'"
def myparam="$params"
// Remove square brackets and whitespace
//myparam = myparam.replaceAll(/\[|\]|\s+/, '')

// Split the string by comma
ArrayList elements = myparam.split(',')
println elements
// Create ArrayList and add elements
def arrayList = new ArrayList(elements)

println arrayList
job('example') {
    parameters {
        booleanParam('FLAG', true)
        choiceParam('OPTION',arrayList)
    }
}