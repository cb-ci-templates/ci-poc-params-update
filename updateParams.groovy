def myparam="$params"
// Split the string by comma
ArrayList elements = myparam.split(',')
// Create ArrayList and add elements
def arrayList = new ArrayList(elements)
println arrayList
job('example') {
    parameters {
        choiceParam('OPTION',arrayList)
    }
}