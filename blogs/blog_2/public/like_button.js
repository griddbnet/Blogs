
const e = React.createElement;

class LikeButton extends React.Component {
  constructor(props) {
    super(props);
    this.state = { liked: false };
  }


  componentDidMount() {

    var xhr = new XMLHttpRequest();

    xhr.onreadystatechange = function () {

	// Only run if the request is complete
	    if (xhr.readyState !== 4) return;

	// Process our return data
	    if (xhr.status >= 200 && xhr.status < 300) {
		    // What do when the request is successful
		    console.log(JSON.parse(xhr.responseText));
	    }
    };

    xhr.open('GET', 'http://localhost:5000/api');
    xhr.send();


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
