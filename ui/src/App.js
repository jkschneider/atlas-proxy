import React, { Component } from 'react';
import CodeMirror from 'react-codemirror';
import {debounce} from 'underscore';
import {Pos} from 'codemirror';
import 'codemirror/mode/groovy/groovy.js';
import 'codemirror/addon/hint/show-hint.js';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/eclipse.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import AtlasGraph from './AtlasGraph';

export default class App extends Component {
  constructor(props) {
    super(props);

    const defaultCode =
`Timer t = select.timer('playback.startLatency')
graph.line(t.latency().axis(0).lineWidth(2))
graph.line(t.throughput().axis(1))

graph.title('Playback Start Latency')
graph.axisLabel(0, 'Latency (seconds)')
graph.axisLabel(1, 'Throughput (requests/second)')`;

    const savedCode = localStorage.getItem('code');
    const savedAtlasUri = localStorage.getItem('atlasUri');

    this.state = {
      code: savedCode ? savedCode : defaultCode,
      atlasUri: savedAtlasUri ? savedAtlasUri : 'https://jon-atlas-cf.cfapps.io'
    };

    this.graph = this.graph.bind(this);
    this.queryAutoComplete = this.queryAutoComplete.bind(this);

    this.updateCode = debounce((newCode) => {
      console.log('updating state with code');
      this.setState({ code: newCode });
      localStorage.setItem('code', newCode);
    }, 200);

    this.updateUri = (uri) => {
      this.setState({ atlasUri: uri });
      localStorage.setItem('atlasUri', uri);
    };
  }

  graph() {
    fetch(`${this.state.atlasUri}/api/graph?width=${this.graphViewport.clientWidth}`, {
      method: 'POST',
      body: this.state.code
    }).then(response => {
      if(response.ok) {
        response.json().then(graphPayload => {
          this.setState({ graph: graphPayload, error: false })
        });
      }
      else {
        // FIXME do something with the error
        console.log(response);
        response.text().then(error => this.setState({ graph: false, error: error }));
      }
    });
  }

  queryAutoComplete(cm, option) {
    const cursor = cm.getCursor();
    const token = cm.getTokenAt(cursor);
    return {
      list: ['select', 'graph'],
      from: Pos(cursor.line, token.start),
      to: Pos(cursor.line, token.end)
    };
  }

  render() {
    return (
      <div className="container" style={{marginBottom: 15}}>
        <div className="header clearfix">
            <h3 className="text-muted">Atlas Query Builder</h3>
        </div>
        <div className="form-group row">
          {/*<label className="col-2 col-form-label">Target Atlas server</label>*/}
          <div className="col-6">
            <div className="input-group mb-2 mr-sm-2 mb-sm-0">
              <div className="input-group-addon">Atlas server</div>
              <input type="text" className="form-control" value={this.state.atlasUri}
                     placeholder="base URI"
                     id="atlasUriSelect" onChange={e => this.updateUri(e.target.value)}/>
            </div>


          </div>
        </div>
        <div className="row">
          <div className="col col-sm-12">
            <CodeMirror
              value={this.state.code}
              onChange={this.updateCode}
              options={{
                mode: 'groovy',
                lineNumbers: true,
                theme: 'eclipse',
                extraKeys: {
                  Tab: (cm) => cm.replaceSelection('  '),
                  'Alt-Enter': this.graph,
                  // 'Ctrl-Space': 'autocomplete'
                },
                hintOptions: {
                  hint: this.queryAutoComplete
                }
              }} />
          </div>
        </div>
        <div className="row justify-content-end">
          <button className="btn btn-primary"
                  onClick={this.graph}>
            Graph (Alt-Enter)
          </button>
        </div>

        <div className="row" ref={g => this.graphViewport = g }>
          {this.state.graph ?
            <div className="col col-sm-12">
              <h3>Result</h3>
              <AtlasGraph graph={this.state.graph} atlasUri={this.state.atlasUri}/>
            </div> : ''}

          {this.state.error ?
            <div className="col col-sm-12 errors">
              <h4>Invalid Query</h4>
              <pre>{this.state.error}</pre>
            </div>  : ''}
        </div>
      </div>
    );
  }
}
