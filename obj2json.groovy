
// common mapping
mapping = [
	0 : ['size': 3, 'src': 'v', 'dst': 'position'],
	1 : ['size': 3, 'src': 'vt', 'dst': 'textcoord'],
	2 : ['size': 3, 'src': 'vn', 'dst': 'normal']
]

def createOutputMesh()
{
	output_mesh = [
		'name' : mesh['name'],
		'position': [],
		'textcoord': [],
		'normal': [],
		'faces': faces
	]

	// for each newly generated vertex
	for (i=0; i<vertexArray.size(); i++) {

		// extract vertex indices from face description 
		// "1/2/3"
		vertexIndex = vertexArray[i].split("/") 
		
		for (a=0; a<vertexIndex.size(); a++) {
			
			idx = vertexIndex[a]

			if (idx.length() > 0) {		

				p = idx.toInteger() - 1
				
				map = mapping[a] 

				for (k=0; k<map.size; k++) {

					output_mesh[map.dst].add(mesh[map.src][map.size * p + k])

				}
			}		
		}
	}

	output_meshes.add(output_mesh)
	//def builder = new groovy.json.JsonBuilder()
	//def root = builder.geometry {
	//	meshes output_mesh
	//}
	//new File(output_mesh.name + ".json").write(builder.toString())
}

// output meshes array
output_meshes = [] 

// source obj file
fileName = args[0]
file = new File(fileName) 

// source geometry array
mesh = ['v': [], 'vt': [], 'vn':[]] 

// "a/b/c" -> vertexIdx
faceVertexMapOnNewVertexIdx = [:]

// array of unique "a/b/c" elements
vertexArray = []

faces = []

Integer meshesCount = 0 

lines = file.readLines() ;

for (int lineNumber=0; lineNumber<lines.size(); lineNumber++) {
	
	line = lines[lineNumber]
 	
 	key = "" 

 	if (line.length() > 0) {

		items = line.split(" ")

		key = items[0]

		if (mesh.containsKey(key)) {
			
			for (i=1; i<items.size(); i++) {

				if (items[i].length() > 0) {

					mesh[key].add(items[i].toDouble())
				}

			}
		}
		else if (key == "f") {
			
			// "a1/b1/c1" "a2/b2/c2" "a3/b3/c3"
			for (i=1; i<items.size(); i++) {
				
				// "ai/bi/ci"
				v = items[i] 

				if (!faceVertexMapOnNewVertexIdx.containsKey(v)) {
					
					faceVertexMapOnNewVertexIdx[v] = vertexArray.size()
					
					vertexArray.add(v)
				}

				// index of new vertex
				faces.add(faceVertexMapOnNewVertexIdx[v])
			}
		}
	}
	
	// if "o"-object detected or end of file
	if (key == "o" || key == "g" || ((lineNumber + 1) == lines.size())) {

		// dump geometry
		if (vertexArray.size() > 0) {
			createOutputMesh() 
		}

		// if its a o key - then reset mesh arrays
		if (key == "o" || key == "g") {
			// reset mesh
			mesh['name'] = items[1]

			// "a/b/c" -> vertexIdx
			faceVertexMapOnNewVertexIdx = [:]

			// array of unique "a/b/c" elements
			vertexArray = []

			faces = []
		}
	}
}

println "Meshes count: " + output_meshes.size() 

println "Source vertices count: " + mesh.v.size() / 3
println "Source texcoords count: " + mesh.vt.size() / 2
println "Source normals count: " + mesh.vn.size() / 3

println "Output vertices count: " + vertexArray.size()
println "Output faces count: " + faces.size() / 3

// export complete geometry
def builder = new groovy.json.JsonBuilder()
def root = builder.geometry {
	meshes output_meshes
}
new File(fileName + ".json").write(builder.toString())
