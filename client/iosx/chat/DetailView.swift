import SwiftUI

struct DetailView: View {

    private let selectedRoom: String
//    var messages: [String] = ["one", "two", "three"]
    @State private var nextMessage: String = ""
    @ObservedObject var resource = Backend.shared
    
    init(room: String) {
        selectedRoom = room
    }

    var body: some View {
        VStack {
            List(resource.messages, rowContent: PostView.init)
            .navigationBarTitle(Text(self.selectedRoom))
            HStack {
                TextFieldContent(key: "Next message", value: self.$nextMessage)
                Button( action: {
                    self.send()
                }){
                    Image(systemName: "paperplane")
                }.padding(.trailing)
            }
        }
    }

    private func send() {
        Backend.shared.sendToPeer(recipient: self.selectedRoom, payload: nextMessage) { success, error in
            print("DetailView sent: success=\(success), error=\(String(describing: error))")
        }
    }
}

struct PostView: View {
    var postModel: PostModel

    var body: some View {
        VStack(alignment: .leading) {
            Text(postModel.envelope.payload).bold()
        }
    }
}

// https://swiftui-lab.com/bug-navigationlink-isactive/
struct MyBackButton: View {
    let label: String
    let closure: () -> ()

    var body: some View {
        Button(action: { self.closure() }) {
            HStack {
                Image(systemName: "chevron.left")
                Text(label)
            }
        }
    }
}

struct DetailView_Previews: PreviewProvider {
    static var previews: some View {
        DetailView(room: "A Room with a View")
    }
}