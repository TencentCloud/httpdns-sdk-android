using UnityEngine;
using System.Collections;

public class GameManager : MonoBehaviour {

	// Use this for initialization
	void Start () {
	
	}

    void Update() {
        if(Input.GetKeyDown(KeyCode.Escape)) {
            Application.Quit();
        }
    }

}
