
const e = React.createElement;

class LikeButton extends React.Component {
  constructor(props) {
    super(props);
    this.state = { liked: false };
  }

    let myHeaders = new Headers();
    myHeaders.append("Content-type", "application/json")

    let requestOptions = {
      method: 'GET',
      headers: myHeaders,
      redirect: 'follow'
    };

    fetch("http://localhost:5000/api", requestOptions)
      .then(response => response.text())
      .then(result => {
        let resp = JSON.parse(result)
        console.log("resp: ", resp)


      })
      .catch(error => console.log('error', error));
  }




  render() {
    if (this.state.liked) {
      return 'You liked this.';
    }

    return e(
      'button',
      { onClick: () => this.setState({ liked: true }) },
      'Like'
    );
  }
}

const domContainer = document.querySelector('#like_button_container');
const root = ReactDOM.createRoot(domContainer);
root.render(e(LikeButton));
